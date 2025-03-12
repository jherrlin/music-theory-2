(ns se.jherrlin.music-theory.webapp.views.abcjs.examples)



(defn view []
  [:div
   [:h2 "ABC examples"]
   (let [url "https://github.com/paulrosen/abcjs/tree/main/examples"]
     [:p
      [:a {:href url} url]])
   (let [url "https://paulrosen.github.io/abcjs/examples/toc.html"]
     [:p
      [:a {:href url} url]])])

(defn routes [deps]
  (let [route-name :abcjs-examples]
    ["/abcjs/examples"
     {:name    route-name
      :view    [view]
      :top-nav :abcjs-examples}]))
