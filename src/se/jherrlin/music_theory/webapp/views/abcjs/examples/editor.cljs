(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.editor
  (:require ["abcjs" :as abcjs]
            [reagent.core :as r]
            [re-frame.alpha :as rf]
            [se.jherrlin.music-theory.webapp.utils :refer [<sub >evt]]))

(def whiskey-abc
  "X: 1
T: Whiskey Before Breakfast
M: 4/4
L: 1/8
K: D
P:A
DD |: \"D\" AEFG A2 FG | ^ABAG F_DEF  | \"G\" GABG \"D\" F2 AF | \"A\" EDEA cefe |
   |  \"D\" ae ge fe ed | dA =cA BA FD  | \"G\" GABG \"D\" F2 AF  \\
   |1 \"A\" EDEF \"D\" D2 DD :|2 \"A\" EDEF \"D\" D2 FE ||")

(def cherokee-abc
  "X: 1
T: Cherokee Shuffle
M: 4/4
L: 1/8
K: D
P:A
DD |: \"D\" AEFG A2 FG |")

(def events-
  [{:n ::abc-str}])

(doseq [{:keys [n s e d]} events-]
  (rf/reg-sub n (or s (fn [db [n']] (get db n' d))))
  (rf/reg-event-db n (or e (fn [db [_ e]] (assoc db n e)))))

(defn editor []
  (new abcjs/Editor
   "abc-editor-id"
   (clj->js {:canvas_id   "abc-canvas-id"
             :warnings_id "abc-editor-warnings-id"
             :abcjsParams {}})))

(defn ui []
  (r/create-class
   {:component-did-mount  #(editor)
    :component-did-update #(editor)
    :display-name         "editor"
    :reagent-render
    (fn []
      (let [abc-str (<sub [::abc-str])]
        [:div
         [:p "Editor example"]
         (let [url "https://paulrosen.github.io/abcjs/examples/editor.html"]
           [:p
            [:a {:href url} url]])
         (let [url "https://github.com/paulrosen/abcjs/blob/main/examples/editor.html"]
           [:p
            [:a {:href url} url]])
         [:textarea {:id         "abc-editor-id"
                     :cols       "80"
                     :rows       12
                     :spellCheck false
                     :value      (or abc-str "")
                     :onChange   (fn [e]
                                   (let [value (.. e -target -value)]
                                     (js/console.log "Editor change")
                                     (js/console.log value)
                                     (>evt [::abc-str (clojure.string/trim value)])))}]
         [:div {:id "abc-editor-warnings-id"}]
         [:div {:id "abc-canvas-id"}]
         [:hr]
         [:button {:on-click #(>evt [::abc-str whiskey-abc])} "Whiskey"]
         [:button {:on-click #(>evt [::abc-str cherokee-abc])} "Cherokee"]]))}))

(defn routes [deps]
  (let [route-name :abcjs-example/editor]
    ["/abcjs/examples/editor"
     {:name    route-name
      :view    [ui]
      :top-nav :abcjs-examples}]))

(comment
  (>evt [::abc-str whiskey-abc])
  (<sub [::abc-str])
  :-)
