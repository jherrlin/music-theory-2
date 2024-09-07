(ns se.jherrlin.music-theory.webapp.views.dev
  )

(defn view [deps]
  [:<>
   [:div "dev"]])

(defn ^:dev/after-load routes [deps]
  (let [route-name :dev]
    ["/dev/dev"
     {:name route-name
      :view [view deps]}]))
