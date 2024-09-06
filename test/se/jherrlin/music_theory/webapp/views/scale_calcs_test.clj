(ns se.jherrlin.music-theory.webapp.views.scale-calcs-test
  (:require [se.jherrlin.music-theory.webapp.views.scale-calcs :as scale-calcs]
            [clojure.test :refer [are deftest is testing use-fixtures]]))


(deftest scale-pattern-entities
  (is
   (=
    (->> (scale-calcs/scale-pattern-entities
          :c
          :mandolin
          #{:ionian :major})
         (sort-by :id))
    '({:id         #uuid "826e9aea-f2de-4a53-a1dd-9fe1ecce1434",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "828a626b-e112-4e1d-ac16-ee8637bc10aa",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "86346eee-2147-490e-b9fa-6f8a02caa9e0",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "8712cd6f-f949-48be-87ab-dd7b6fb4a8c2",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "873e360a-227d-414c-8682-78320bd17dd1",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "883d1f00-0970-41e0-bc01-6f8dc1ec3176",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "89197b16-211c-4b44-8f61-9846210856c0",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "899bf840-b1b9-4226-8aae-ccd8a4230262",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "8a76ac69-26e6-437c-8c8a-5f9d800e8256",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "8bc4c1b4-ef7d-4930-ba16-c9832abebffc",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "8de8f1b8-6028-4a4a-8012-9b0351894949",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "8eb1c852-017c-4fc7-a90d-b15caf7da751",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "961a021c-3c10-48fe-8d6e-e580697bf953",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "976c12ac-43ae-4979-8a79-ea69c6643ed3",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "98c2a8c0-8e7f-46d3-baab-ed80fbc9a65b",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "9943e506-1aa3-4eaa-a14f-8d5b680096a4",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "99fa6c1b-fe7e-403b-80c8-e92350be214f",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "9cbc8440-4972-4d23-b26d-2c300e8e73a6",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "a505c07b-dd29-47a6-9e4d-c79468f3da2a",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "a542418c-3667-426e-82a6-a4d3886deb2c",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "a97f4133-1097-495a-8019-be7af748ae4d",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "a99a6847-f41a-42c9-a068-8d86a3a6eb74",
       :instrument :mandolin,
       :key-of     :c}
      {:id         #uuid "abe53b98-33ab-417c-99ae-b301230cbd26",
       :instrument :mandolin,
       :key-of     :c}
      {:id #uuid "ac04bc83-478f-47a4-be4e-5608beffb277",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "ac4cac1b-4247-4e85-90d6-3c2fd3dffa45",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "aed3e7ce-31da-4e98-b5cc-a396a5b980bf",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "af427ba7-fabc-4d54-b816-379c6a8845a3",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "afdeb3a9-ee24-4fe9-b766-fd088d4b0ec5",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "b1fff55e-0c58-4523-b11e-4a75eef38b70",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "b2681b2a-4685-4aa8-b675-88b03da4b0f3",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "b51d6d59-b2eb-4862-bcbd-dd46a2198873",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "b9f5cfa6-d97a-4164-95b1-440c46daaecf",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "bcd4ecf0-b69e-45c8-84ad-a12cb257a0b0",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "bcf4fcee-938a-4d41-9285-5da029e50859",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "be800b03-36d6-4f5b-a879-d3af632e429a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "c6dbe3e1-a43b-47c2-a880-4482c15252f8",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "ca72c2c6-d522-4eae-b0bf-d7a60549dd8a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "cb1b2226-b57a-478c-bb01-bec1aec3201e",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "cc3d4a21-2a2d-46e5-8cea-20caaf13c49e",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "ce13cdd1-1a00-4019-8f4e-ec89855908fe",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "d08d4714-ddf7-41c1-aca4-8057adaec3af",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "d2c6b1cf-f840-4852-9c1a-1d931ebb960a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "d565356e-7563-45de-960f-7d9bef9e1e9b",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "d886da54-dfd1-4e53-b32e-df64c35b84ed",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "db803aa1-86f4-414c-b2ed-f87b2de0206e",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "de269f68-f54a-4af0-a4a7-6eb43236481c",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "e001436e-8c7a-4125-a1ae-a8847a451d7b",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "e2873eba-8e16-4741-b6f4-2c12200ddf10",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "e3437a0f-c7a1-4abb-9f7b-af0940f20f14",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "e3b5bf11-127f-4cf1-8a18-f72569b8b4a8",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "e41c39f7-3c0d-41f6-91cf-05f5a10f4eec",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "e728c3bf-481e-479f-8335-1bd95faa8fd0",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "eea5155d-5f5f-435b-8201-56e889d40a4a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "f1445ef5-910a-4278-ab2b-9cfc1cb62202",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "f5eccb6d-d8f9-44d1-a24c-c067398b29cd",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "f77eb757-9140-4c61-bdbd-92ff8f4cf1ce",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "f95326aa-6061-4b11-8dad-0785e1114a73",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "fa26950d-fadf-4a49-a12e-73f3439c978a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "fb738fa2-5a19-4fcd-8522-379a58890238",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "fbb29ae2-c0d7-44f9-94e9-6923b787e8de",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "fd493ee0-2005-44fc-84b5-add5ac41151a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "fd8fcd0e-0fe4-4c59-a0ba-d71480d43abe",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "01762a0a-e6db-44cc-acf1-0a5b39a0cbd5",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "01c7efae-191c-4c12-9f7e-ac6aaeec2349",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "03bd6274-eacb-4218-a92a-0309b708934f",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "077881e5-45a8-4525-bcf8-846a79dc163a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "0ac8982a-079b-42f0-af24-5ab70712ca5c",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "0adb66d6-34b0-487b-8d2a-605543a47cf8",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "0b86630b-d1f0-4f5f-88fb-e5a03d2ec4c2",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "0bc1a952-0a98-444a-bdb6-08a7ba1bda89",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "0d0789a2-3602-49cd-a396-7600a7daae5a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "0d3d7efc-1237-43ec-8b08-458c7ce08a2f",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "0e615aa7-09ef-402c-9bf5-385983cff6ca",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "12312f5f-2fb7-4b05-bfec-263691804c0a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "13fefbd0-f08f-487c-a7d6-11ad59113401",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "159d8b3c-8d25-422c-8f56-7a23d9a3a222",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "1aa206c2-778f-4de2-ae96-cb3c691b87a3",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "1cc41d86-9145-4160-a8dd-ef1c0759581b",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "1ce45420-e9b3-437d-8446-01a6ae4aa0d2",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "21bb2a50-fb36-4461-8f70-de908b983147",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "24b126e7-920e-4e2f-90b4-ff31d36d27f3",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "2647ea4a-e4dc-44ed-b383-b638e8e7e8d4",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "28b02aaa-3d31-40ca-851d-5a6f0425a03e",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "2b759637-4591-478b-bed1-f93fe347d011",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "2d2e9036-db68-4881-b6c0-e9ebb04c8eb2",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "2e0d6ebb-ddb7-4ba7-b862-8b73ddcd668f",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "2fbffdf0-90d6-4c03-a30b-73109c2c733f",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "35975438-3375-4d6b-9dad-166f6a8969cf",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "3a4429d7-26bb-4707-9cd9-f38e1387f8b4",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "3a9bc975-2082-4075-b123-0015c5ad0994",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "42ef1698-855c-438a-927d-e0fd6124d91d",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "43f1c99f-3143-42c0-a30a-184d9e66ab68",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "44f2731a-49d9-41d3-ad97-0f8f7479247b",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "4921687c-dc99-4ebd-b9a1-4069721f44c9",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "498cd0f9-6b0a-4bef-83f5-cfa9628930ce",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "4a841026-5e36-490c-b83c-03a183da6db0",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "4e847826-ee33-476a-99aa-f1feebf86a89",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "50b0d3e8-aa16-45fd-a23c-a4cced3bcf7c",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "527faa4e-3b21-4ddc-b52c-0962548b16b3",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "53cdec3d-5af7-406a-9da6-906223a4f6bd",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "53e82214-da90-4fad-8e5e-a69a9a77ac6a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "5406fad2-8c20-4db9-8a45-9932cd88a36a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "54d58e70-68a4-4984-8b73-f41f5d871f65",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "5628c601-e16f-4b0c-bc5e-b05f368c5d0f",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "584e8cdd-d171-4009-aff3-8f8cfd0263e9",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "5865c0c5-de11-4319-a139-4488f779c505",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "5a6db315-d2f8-464e-a83a-ab6571cfc873",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "5b978a1d-a861-45c3-90a4-7c7078ca903c",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "5c9ee636-1dd3-4e10-af3c-79b9cd20b422",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "5ebb965e-304d-40a5-869e-b01effca0e70",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "5f21794f-5e56-47f3-8330-59675747b1e9",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "6146e78b-a300-4a8c-8702-fcb8ebbccb31",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "62084d2f-631f-4a0e-a656-4b79ad2371ff",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "621bbb40-aaea-439d-baf6-90f38b640fff",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "62b67ee8-b41d-40eb-a7f8-6b6d413163d3",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "656474e7-fc5f-43ca-afff-c4a49923da6c",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "6803ce3a-4dd9-4988-b6df-3d27368421d2",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "68321a7d-2670-46f7-87ae-f5c8f1c86f17",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "6b27ad33-7ea0-426f-bac8-52b8b2c98e60",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "6b8bad80-6252-46d6-a667-c25d3c313334",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "6b9fd9a8-ae2d-47b4-9f8f-bfa31169b9db",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "6d2ab41b-2e88-446f-9cd0-71043dedfba6",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "6ecd4bf9-2cd4-4a71-b612-b0d5b1ae4481",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "6f7393f4-2fa7-4425-a438-b52ded57336c",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "7343c79f-636b-456b-824e-8d48274e1e29",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "75a93614-7d64-4a9b-8e89-793b594577d2",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "76526382-261b-44e6-bbbe-ffd46e9a0ba2",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "76659a78-8b5b-4dc7-84de-db6cf210305f",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "7683f242-6df9-4994-8c3c-ab76f3ac8b29",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "7699caf1-35df-40ef-b916-959ad787176a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "76fab7e7-0a91-458e-be07-abd794498b7a",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "76ff1483-2723-4d70-8ff3-84767d5d608d",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "7751ae2d-6b20-411c-8c0e-683a341c0cd8",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "7a8f500e-387c-4a72-814c-719adf8db2b6",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "7c40d6df-704e-46a8-880b-0d9611dcc51d",
       :instrument :mandolin,
       :key-of :c}
      {:id #uuid "7e8d0a66-3a67-4c5d-b8c3-7a2b2d401089",
       :instrument :mandolin,
       :key-of :c}))))
