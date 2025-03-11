(ns se.jherrlin.music-theory.webapp.views.abcjs.examples)



(defn view []
  [:div
   [:p
    [:a {:href "https://paulrosen.github.io/abcjs/examples/toc.html"}
     "https://paulrosen.github.io/abcjs/examples/toc.html"]]]
  )

(defn routes [deps]
  (let [route-name :abcjs-examples]
    ["/abcjs/examples"
     {:name    route-name
      :view    [view]
      :top-nav :abcjs-examples}]))
