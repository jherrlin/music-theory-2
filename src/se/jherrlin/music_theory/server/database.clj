(ns se.jherrlin.music-theory.server.database
  (:require [datomic.client.api :as d]))

(defonce client
  (d/client {:server-type :datomic-local
             :system      "musik-theory"
             :storage-dir "/home/nils/git/jherrlin/music-theory-2/storage/databases"}))

(def Document [{:db/ident       :document/id
                :db/cardinality :db.cardinality/one
                :db/valueType   :db.type/uuid
                :db/unique      :db.unique/identity}
               {:db/ident       :document/title
                :db/cardinality :db.cardinality/one
                :db/valueType   :db.type/string}
               {:db/ident       :document/version
                :db/cardinality :db.cardinality/one
                :db/valueType   :db.type/long}
               {:db/ident       :document/components
                :db/cardinality :db.cardinality/many
                :db/valueType   :db.type/ref}])

(def Component [{:db/ident       :component/id
                 :db/cardinality :db.cardinality/one
                 :db/valueType   :db.type/uuid
                 :db/unique      :db.unique/identity}
                {:db/ident       :component/type
                 :db/cardinality :db.cardinality/one
                 :db/valueType   :db.type/keyword}
                {:db/ident       :component/version
                 :db/cardinality :db.cardinality/one
                 :db/valueType   :db.type/long}
                {:db/ident       :component/data
                 :db/cardinality :db.cardinality/one
                 :db/valueType   :db.type/string}])

(def datomic-schema (concat Document Component))

(defn init [client schema]
  (d/create-database client {:db-name "musik-theory"})
  (let [conn (d/connect client {:db-name "musik-theory"}) ]
    (d/transact conn {:tx-data schema})
    conn))

(def conn (init client datomic-schema))

(comment
  (d/transact conn {:tx-data
                    [{:document/id      #uuid "3378e81a-0c56-4a2d-a2c2-cbff03829eae"
                      :document/title   "First document"
                      :document/version 1
                      :document/components
                      [{:component/version 1
                        :component/type    :h2-title
                        :component/id      #uuid "2e0d5b61-319e-488f-a1e7-690f1bbee468"
                        :component/data    (pr-str {:text "Whiskey before breakfast lesson"})}
                       {:component/version 1
                        :component/type    :textarea
                        :component/id      #uuid "8d75e4e1-6963-4110-bca4-3a05bbffd863"
                        :component/data    (pr-str {:text "hejsan"})}
                       {:component/version 1
                        :component/type    :abc-editor
                        :component/id      #uuid "c5b7748e-9d60-4602-ac4c-61cc488e6270"
                        :component/data    (pr-str {:instrument :guitar
                                                    :abc-str    "abc-str"})}
                       {:component/version 1
                        :component/type    :abc-editor
                        :component/id      #uuid "3cf5d204-15b2-4e6c-9c1e-4094f54868ca"
                        :component/data    (pr-str {:instrument :mandolin
                                                    :abc-str    "derp dep"})}]}]})

  (d/transact
   conn
   {:tx-data [[:db/add "d1" :document/id         #uuid "672f67ef-bebd-4199-9b71-7a4fbf68582a"]
              [:db/add "d1" :document/title      "A title"]
              [:db/add "d1" :document/version    0]
              [:db/add "c1" :component/id        #uuid "932bb15f-7b29-4cbd-88f9-50c279b2f732"]
              [:db/add "c1" :component/data      "component 1 data"]
              [:db/add "c1" :component/version   1]
              [:db/add "c1" :component/type      :some-component-type]
              [:db/add "c2" :component/id        #uuid "2d364740-f8f0-4151-8104-2e7673291b04"]
              [:db/add "c2" :component/data      "component 2 data"]
              [:db/add "c2" :component/version   1]
              [:db/add "c2" :component/type      :some-component-type]
              [:db/add "d1" :document/components "c1"]
              [:db/add "d1" :document/components "c2"]]})

  (d/q '[:find  ?d ?title ?components
         :where [?d :document/id]
                [?d :document/components ?components]
         [?d :document/title ?title]]
       (d/db conn))

  (d/pull
   (d/db conn)
   [:document/id
    :document/title
    :document/version
    {:document/components
     [:component/id
      :component/version
      :component/type
      :component/data]}]
   [:document/id #uuid "3378e81a-0c56-4a2d-a2c2-cbff03829eae"])

  #:document{:id      #uuid "672f67ef-bebd-4199-9b71-7a4fbf68582a",
             :title   "A title",
             :version 0,
             :components
             [#:component{:id      #uuid "932bb15f-7b29-4cbd-88f9-50c279b2f732",
                          :version 1,
                          :type    :some-component-type,
                          :data    "component 1 data"}
              #:component{:id      #uuid "2d364740-f8f0-4151-8104-2e7673291b04",
                          :version 1,
                          :type    :some-component-type,
                          :data    "component 2 data"}]}

  )
