(ns se.jherrlin.music-theory.server.routes
  (:require
   [taoensso.sente :as sente]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as ring.coercion]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.middleware.anti-forgery]
   [ring.middleware.keyword-params]
   [ring.middleware.params]
   [ring.middleware.session :as session]
   [ring.middleware.session.memory :as memory]
   [se.jherrlin.music-theory.server.pages :as pages]
   [taoensso.timbre :as timbre]
   [se.jherrlin.music-theory.server.database :as server.database]))

(defn- get-sch-adapter []
  (when-let [server-name
             (cond
               (resolve 'org.httpkit.server/run-server)
               'http-kit

               (resolve 'nginx.clojure.embed/run-server)
               'nginx-clojure

               (resolve 'aleph.http/start-server)
               'aleph)]

    (let [sym (->> server-name (str "taoensso.sente.server-adapters.") symbol)]
      (require sym)
      ((resolve (symbol (str sym "/get-sch-adapter")))))))

(defn reitit-args []
  {:exception pretty/exception
   :data
   {:middleware
    [parameters/parameters-middleware

     ;; used by Sente
     ring.middleware.params/wrap-params
     ring.middleware.keyword-params/wrap-keyword-params]}})

(defn fetch-document [{:keys [?reply-fn ?data] :as event}]
  (let [document (server.database/pull-document ?data)]
    (when ?reply-fn
      (?reply-fn "Hejsan"))))

(defn fetch-documents [{:keys [?reply-fn ?data] :as event}]
  (let [documents (server.database/pull-documents)]
    (if (and ?reply-fn documents)
      (?reply-fn documents)
      (timbre/error "`?reply-fn` or `documents` not present"))))

(defn ws-event-handler [{:keys [id] :as event}]
  (case id
    :fetch/document  (#'fetch-document event)
    :fetch/documents (#'fetch-documents event)
    (timbre/error "Don't know how to hadle `ws-event` with id: " id)))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket-server!
       (get-sch-adapter)
       {:packer     :edn
        :user-id-fn :client-id})]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-recv                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom

  (sente/start-server-chsk-router! ch-recv ws-event-handler))

(comment
  @connected-uids
  )

(defn websocket-endpoints []
  [""
   ["/websocket/chsk" {:get  ring-ajax-get-or-ws-handshake
                       :post ring-ajax-post}]])

(defn index-page []
  ["/" {:summary "Index page"
        :get     (fn [request]
                   {:status 200
                    :body   (pages/index-html request)})}])

(defn health []
  ["/health"
   {:get (fn [_req] {:status 200 :body "ok"})}])

(defonce session-stores-storage (atom {}))
(defonce session-store (memory/memory-store session-stores-storage))

(comment
  (chsk-send! "42229e44-47b2-4fda-b845-e21c76c94072" [:a/derp :hehe])
  )

(defn routes-with-required-csrf [& routes]
  (into
   ["" {:middleware [[session/wrap-session {:store session-store}]
                     ring.middleware.anti-forgery/wrap-anti-forgery]}]
   (into [] routes)))

(defn handler [req]
  ((ring/ring-handler
    (ring/router
     [(routes-with-required-csrf
       (index-page)
       (websocket-endpoints))
      (health)]
     (reitit-args))
    (ring/routes
     (ring/create-resource-handler {:path "/"})
     (ring/create-default-handler {:not-found (constantly {:status 404 :body "Not found"})})))
   req))
