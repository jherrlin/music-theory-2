(ns se.jherrlin.music-theory.server.pages
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [hiccup.page :refer [html5 include-css include-js]]
   [taoensso.timbre :as timbre]))


(defn- read-resource-edn-file
  "Read file from filesystem and parse it to edn."
  [resource-filesystem-path]
  (try
    (-> resource-filesystem-path io/resource slurp edn/read-string)
    (catch java.io.IOException e
      (timbre/error "Couldn't open" resource-filesystem-path (.getMessage e)))
    (catch Exception e
      (timbre/error "Error parsing edn file" resource-filesystem-path (.getMessage e)))))

(defn index-html
  "Create an index page with a CSRF token attached to it.

  This is mainly used if you wanna use re-frame as a frontend."
  [req]
  (html5
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name    "viewport"
            :content "width=device-width, initial-scale=1"}]
    [:link {:rel "shortcut icon" :type "image/x-icon" :href "favicon.ico"}]
    [:link {:rel "stylesheet" :type "text/css" :href "abcjs-audio.css"}]]
    [:body {:style "height: 100%"}
     [:div#app {:style           "height: 100%"
                :data-csrf-token (:anti-forgery-token req)} "loading..."]
    (->> "public/js/manifest.edn"
         (read-resource-edn-file)
         (map :output-name)
         (mapv #(str "js/" %))
         (apply include-js))]))
