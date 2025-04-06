(ns se.jherrlin.music-theory.webapp.websocket
  (:require
   [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]
   [taoensso.sente :as sente :refer [cb-success?]]
   [re-frame.alpha :as re-frame]
   [taoensso.timbre :as timbre]))



(def app-db-path ::websocket)

(defn path [x]
  (-> [app-db-path x] flatten vec))

(def open?-path (path ::open?))
(def ever-opened?-path (path ::ever-opened?))
(def type-path (path ::type))
(def uid-path (path ::uid))

(def events-
  [{:n ::open?
    :p open?-path}
   {:n ::ever-opened?
    :p ever-opened?-path}
   {:n ::type
    :p type-path}
   {:n ::uid
    :p uid-path}])

(doseq [{:keys [n s e p]} events-]
  (re-frame/reg-sub n      (or s (fn [db _]     (get-in   db p))))
  (re-frame/reg-event-db n (or e (fn [db [_ e]] (assoc-in db p e)))))

(re-frame/reg-event-db
 ::update-state
 (fn [db [_ {:keys [type open? ever-opened? uid] :as state}]]
   (-> db
       (assoc-in type-path type)
       (assoc-in open?-path open?)
       (assoc-in ever-opened?-path ever-opened?)
       (assoc-in uid-path uid))))

(defn start! [csrf-token incomming-events-handler]
  (timbre/info "Starting websocket with Sente.")
  (let [{:keys [chsk ch-recv send-fn state] :as m}
        (sente/make-channel-socket! "/websocket/chsk" csrf-token {:type :auto})
        ret (sente/start-client-chsk-router!
             ch-recv
             (fn [{:keys [id state] :as event}]
               (timbre/debug "Incomming event:" event)
               (case id
                 :chsk/state (>evt [::update-state @state])
                 (incomming-events-handler event))))]

    (re-frame/reg-fx
     ::send
     (fn [{:keys [data on-failure on-success timeout]
           :or   {timeout    5000
                  on-failure #(timbre/error "ws error:" %)}}]
       (if-not on-success
         (send-fn data)
         (send-fn data timeout
                  (fn [cb-reply]
                    (if (cb-success? cb-reply)
                      (on-success cb-reply)
                      (on-failure cb-reply)))))))

    (re-frame/reg-event-fx
     ::send
     (fn [{:keys [db]} [_event-id {:keys [data on-failure on-success timeout] :as args}]]
       (if (get-in db open?-path)
         {::send args}
         (do
           (timbre/error "Websocket is not open")
           {}))))

    (re-frame/reg-fx
     ::reconnect
     (fn [_]
       (sente/chsk-reconnect! chsk)))

    (re-frame/reg-fx
     ::disconnect
     (fn [_]
       (sente/chsk-disconnect! chsk)))))

(comment
  (get @re-frame.db/app-db ::websocket)

  (<sub [::open?])
  (<sub [::ever-opened?])

  (>evt [::send {:data       [:fetch/documents]
                 :on-success #(timbre/info "on-success" %)
                 :on-failure #(timbre/info "on-failure" %)}])

  )
