(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.basic
  (:require ["abcjs" :as abcjs]
            [reagent.core :as r]))

(def whiskey-abc
  "
X: 1
T: Whiskey Before Breakfast
M: 4/4
L: 1/8
K: D
P:A
DD |: \"D\" AEFG A2 FG | ^ABAG F_DEF  | \"G\" GABG \"D\" F2 AF | \"A\" EDEA cefe |
   |  \"D\" ae ge fe ed | dA =cA BA FD  | \"G\" GABG \"D\" F2 AF  \\
   |1 \"A\" EDEF \"D\" D2 DD :|2 \"A\" EDEF \"D\" D2 FE ||
")

(defn render-abc []
  (abcjs/renderAbc
   "whiskey-score"
   whiskey-abc
   {:responsive "resize"}))

(defn ui []
  (r/create-class
   {:component-did-mount  #(render-abc)
    :display-name "scores"
    :reagent-render
    (fn []
      [:div
       [:p "Basic example that renders static ABC"]
       [:p
        [:a {:href "https://paulrosen.github.io/abcjs/examples/basic.html"}
         "https://paulrosen.github.io/abcjs/examples/basic.html"]]
       [:p
        [:a {:href "https://github.com/paulrosen/abcjs/blob/main/examples/basic.html"}
         "https://github.com/paulrosen/abcjs/blob/main/examples/basic.html"]]
       [:div {:id "whiskey-score"}]])}))

(defn routes [deps]
  (let [route-name :abcjs-example/basic]
    ["/abcjs/examples/basic"
     {:name    route-name
      :view    [ui]
      :top-nav :abcjs-examples}]))
