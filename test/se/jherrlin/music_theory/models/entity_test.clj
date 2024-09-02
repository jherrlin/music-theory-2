(ns se.jherrlin.music-theory.models.entity-test
  (:require [se.jherrlin.music-theory.models.entity :as models.entity]
            [clojure.test :refer [are deftest is testing use-fixtures]]))


(deftest str-to-entities
  (is
   (=
    (models.entity/str-to-entities
     "guitar,c,94f5f7a4-d852-431f-90ca-9e99f89bbb9c")
    [{:instrument :guitar,
      :key-of     :c,
      :id         #uuid "94f5f7a4-d852-431f-90ca-9e99f89bbb9c"}])))

(deftest entities-to-str
  (is
   (=
    (models.entity/entities-to-str
     [{:instrument :guitar,
       :key-of     :c,
       :id         #uuid "94f5f7a4-d852-431f-90ca-9e99f89bbb9c"}
      {:instrument :guitar,
       :key-of     :c,
       :id         #uuid "94f5f7a4-d852-431f-90ca-9e99f89bbb9c"}])
    (str
     "guitar,c,94f5f7a4-d852-431f-90ca-9e99f89bbb9c"
     "_"
     "guitar,c,94f5f7a4-d852-431f-90ca-9e99f89bbb9c")))

  (is
   (=
    (models.entity/entities-to-str
     [{:instrument :guitar,
       :key-of     :c,
       :id         #uuid "94f5f7a4-d852-431f-90ca-9e99f89bbb9c"}])
    "guitar,c,94f5f7a4-d852-431f-90ca-9e99f89bbb9c")))
