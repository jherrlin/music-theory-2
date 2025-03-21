(ns se.jherrlin.music-theory.webapp.views.abcjs.examples.parser
  (:require [clojure.string :as str]
            [instaparse.core :as insta]))


(def whiskey-abc
  "X: 1
T: Whiskey Before Breakfast
K: D
L: 1/8
K: D
P: A
DDC,,,ab'''
")

(def all-pitches
  ["C,,," "D,,," "E,,," "F,,," "G,,," "A,,," "B,,,"  ;; octave 0
   "C,," "D,," "E,," "F,," "G,," "A,," "B,,"         ;; octave 1
   "C," "D," "E," "F," "G," "A," "B,"                ;; octave 2
   "C" "D" "E" "F" "G" "A" "B"                       ;; octave 3
   "c" "d" "e" "f" "g" "a" "b"                       ;; octave 4
   "c'" "d'" "e'" "f'" "g'" "a'" "b'"                ;; octave 5
   "c''" "d''" "e''" "f''" "g''" "a''" "b''"         ;; octave 6
   "c'''" "d'''" "e'''" "f'''" "g'''" "a'''" "b'''"] ;; octave 7
  )


(->> all-pitches
     (map #(str/replace % #"'" "\\'"))
     (str/join "|"))



(def parser
  "ABC = STMT (<NL> STMT)* <NL>?
<STMT> = number | title | key-of | part | header | tones

number   = <BOL> <'X:'> <SPACE>? INT
title    = <BOL> <'T:'> <SPACE>? TO-EOL
key-of   = <BOL> <'K:'> <SPACE>? TO-EOL
part     = <BOL> <'P:'> <SPACE>? TO-EOL
header   = <BOL> #'[^XTKP]:' <SPACE>? TO-EOL
tones    = (ACC? TONE)*


ACC          = #''
TONE         = #'C,,,|D,,,|E,,,|F,,,|G,,,|A,,,|B,,,|C,,|D,,|E,,|F,,|G,,|A,,|B,,|C,|D,|E,|F,|G,|A,|B,|C|D|E|F|G|A|B|c|d|e|f|g|a|b|c\\'|d\\'|e\\'|f\\'|g\\'|a\\'|b\\'|c\\'\\'|d\\'\\'|e\\'\\'|f\\'\\'|g\\'\\'|a\\'\\'|b\\'\\'|c\\'\\'\\'|d\\'\\'\\'|e\\'\\'\\'|f\\'\\'\\'|g\\'\\'\\'|a\\'\\'\\'|b\\'\\'\\''
BOL          = #'^'
SPACE        = #'\\s+'
INT          = #'-?\\d+'
TO-EOL       = #'[^\\s+].*'
NL           = #'\\s*\\r?\\n'")


(defn ui []
  [:div
   [:h2 "Instaparse"]
   [:div {:style {:display "flex"}}
    [:textarea {:spellCheck false
                :rows       9
                :style      {:flex "1"}
                :value      whiskey-abc}]]
   [:pre
     (with-out-str
       (cljs.pprint/pprint
        (->> whiskey-abc
             ((insta/parser parser)))))]])

(defn routes [deps]
  (let [route-name :abcjs-example/instaparse]
    ["/abcjs/examples/instaparse"
     {:name    route-name
      :view    [ui]
      :top-nav :abcjs-examples}]))
