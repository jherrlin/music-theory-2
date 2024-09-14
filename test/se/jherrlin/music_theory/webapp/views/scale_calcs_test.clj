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
    '({:id         #uuid "811ec70c-f917-442c-a184-526af217440c",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "81dca095-541a-440a-8dc9-de30270dfc12",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "8378024e-620d-4347-ba85-87891e7f586d",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "87e6ca98-5706-4626-9cc1-04267beb67a9",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "884e6e32-8aae-4601-8be9-b2eb944f7cfe",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "8980fd41-c527-4549-883f-f6f86b171e85",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "8b44e640-7228-48c8-ab5e-b483db1076c0",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "8e16f988-431b-4c5d-bf19-b9bb6303c166",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "8e977915-e1e2-438c-8446-b3451825c110",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "91ad088a-79dc-4cea-930f-bcdb9aa620ef",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "9449b2c9-e856-4dd1-98cc-b37a808de545",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "9771f8e1-3d59-425d-9807-62a2ad4923a0",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "979656e5-cde0-49f2-8649-078c884da442",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "9802dac1-0ddb-4155-9560-c911285517af",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "9878c8a1-84e1-48ca-b35a-34b48589015d",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "9a40ec2f-abad-40d1-9f6d-04565b7cc659",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "9d6b8747-23f6-40ff-b92d-57885808dad0",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "9db1ff02-79f4-4f91-ad93-e71cef27c138",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "a0b0062e-b809-4f73-83a9-7e81772e743d",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "a14c5e6f-dec5-4dda-a3eb-e11bae9fd2d1",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "a3321fd6-5cca-440c-a574-66d438fc9495",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "a774f5af-d3f3-4d91-9502-7461266e5627",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "a7c7ae84-6048-4c4a-bd5f-ec193979bd2e",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "a8b99df2-7fb8-441e-9c3a-916f401c7865",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "a93c19b5-bb29-420e-9e11-bfcbd6070fd5",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "aa32f86a-4cfd-4610-8742-4a9790d76d3c",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "ac2982f4-ce36-4b14-8361-9a1c088f6c37",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "aec96ff5-af68-4d14-85e9-eb5c66f3d80f",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "b141312c-882e-4e6e-863a-2616e3c75d17",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "b4867394-a045-419a-8ab8-706ba8e8c9c6",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "b5e6ed63-2cde-4353-ba35-3363ee3b77fd",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "b7cc00bd-2dbf-4034-92c2-ba38653bf345",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "ba0ef14f-a89a-4fbb-9ea1-c14e2e42bdc5",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "bbe3271a-f8db-4e7f-84ea-9c2254ade75f",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "bbfc59cf-53c1-4d50-9d21-161916ec8382",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "bdde0a72-cd89-4e0c-8d56-40f09812e645",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "bf9b840b-5bc6-4eae-a701-c2bee9ab2c7b",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "c25ef3a7-58af-4644-a9c9-6b53d5b8e877",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "c268d48a-6f2d-45d2-8157-9b85ef0f23ed",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "c45ac8d9-a69f-46fa-a518-ae23b1f0dbfa",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "c6154d58-953d-4c5c-bfe4-f4a6b534dca9",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "c9c65b74-ed76-4eea-829a-5fdb07d34cbd",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "cd92d68d-ca9e-45b7-b530-e9a48e70e4dd",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "cfa51275-d382-45c8-806f-1e2e0093bd9a",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d3e994e9-cf99-4868-820f-e105472a8b66",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d4dfe214-450c-486b-979f-5afa9a51f592",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d5487e12-6f5c-4af8-8e9a-4f4df99c3ba2",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d5efbc0b-8377-4dbe-a6b5-0454d225ae19",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d62a7660-8fae-4843-b824-fbdffb187e4a",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d6dd4b0d-3f87-4b70-a1d8-97d23b58432a",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d7cd3a65-40c9-4237-96c6-223bb399130b",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d8305325-fc98-42b3-a82e-8aa5b0337e1b",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "d92cccff-e669-4e85-a867-38d250e8983d",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "daef4e4c-bda8-4492-88b7-f3a79e668fbf",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "db4d532d-6b31-4359-a69f-6381bb837e41",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "de1eb0e6-38cf-40e5-8512-7f0236e7bf0f",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "e2c4bf81-b4de-459b-9fdc-e80e7196e53a",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "e4f8de5d-3b3a-4df0-a0fa-140586a24ee9",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "e58b5e5b-b89a-49b8-9c1c-eac98dd107ab",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "e890f709-e10e-4770-bdde-e50d286de888",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "e99e0f40-93df-4524-b1f8-e6c70b12972f",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "e9b15cef-3049-4945-8966-24619cdf6fe6",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "ea2228d5-0c05-4da0-9224-c48c1be01994",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "ea86ea3a-88b0-4b0b-aeaa-a03a529c08d5",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "ecc69d9c-2c2f-48a5-aaf1-5e874d98e56b",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "ee19cdea-aaa4-4a22-94ac-8766de0f812d",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "efb572b2-4f39-4430-a671-6cffc9bc6c0f",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "f1d28e95-464b-4af3-be09-a5e4a846a450",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "f4be8ca2-d194-481a-9db6-a08d6b5396f8",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "f58f600b-8d27-4a92-9482-0f77ddbc79cf",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "f66a28fe-3744-4948-b47b-7a8881a63fd5",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "f746a04a-671b-45c9-8edb-9e04b44e610e",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "f7c3330a-1371-42b2-86bf-61a7650860b8",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "f9ba20f7-45be-44da-971d-82ce4ff135e8",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "fb333ffa-0f62-47a1-b10a-3927c82dd3c3",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "fddb91d2-e9b0-40a4-8380-001ec07832b7",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "ff3fb80a-1d55-4fb7-b6ec-7855c8524362",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "ffe0605a-38de-4148-887b-f27296b69615",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "020a07fc-c865-4efc-a1a0-acd056ca2c29",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "054ce2f7-92a9-4a04-b711-530e3cdd58d8",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "07fad13b-8c21-4235-8556-e539f9363a49",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "09bfc2da-5e92-4842-8803-0995913896c2",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "0e989d46-19d5-419f-9571-b2dabd2245eb",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "121ab152-9c59-4295-867a-8c5e0c0ed506",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "13efd73f-0908-41cb-96cb-863176e605c3",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "14c874e0-7567-4a5f-ab7a-1f5bfdddb1da",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "155d42df-bc8c-4edb-9e1d-36907f468ecd",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "1aa21e75-2b39-4031-abc3-30e58500349c",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "1ab5be94-b980-4660-b6e0-65672107ed86",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "1c696d6f-9ff3-448b-b6a9-c4c5e34656a7",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "1cd77b0a-5474-4f51-adb3-a0bdb754d1db",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "1e3d1db3-1d09-4d1d-9301-337d60393731",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "1f274cd7-e6a3-414a-87f8-8634fcc83b6d",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "2066c30b-b532-40c1-ab7f-fc3a6c9be5a0",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "2405dfc3-5191-45c9-ba6f-2f3d93369f59",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "2440562d-30d7-4dfe-8202-da9d53ef3591",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "25354a4e-5efa-419e-b1d5-deb5761730ee",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "25b46d30-8876-4520-b68d-ea88298522f9",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "2d73a6c5-8fe6-402c-a311-b364079f80ec",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "2e02f556-374f-4a47-a9b1-3d17a3570c72",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "30c85c22-55de-4363-ab8c-c5f3c40438c8",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "34decca4-2db1-44cd-8ddc-bb1448203ed7",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "35c09ffc-02cf-4ee6-a5c0-793ae919ba86",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "3719d067-e777-43f2-a3fc-5d6af44bb797",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "3870d9b1-f932-47e5-ac15-704685d15ef1",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "3993df0a-bf5a-478a-88bb-0eee837b5801",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "39bbd1ac-f657-4fe3-b2e0-484f98aad506",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "3b6c924b-097c-4746-964c-1136d9bdbcd5",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "471ad1a7-dea2-4261-8ae2-a394249a0359",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "49268668-d39a-4558-9b0d-b842057c9cb9",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "49735d6a-6a0d-434c-828f-8eb5955fadfd",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "4c30cbaf-53a0-4d2f-ba53-b19b5e9083d1",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "4c91ecb1-c778-4f22-9d11-98230d8d3caf",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "508f0524-d41c-405a-894d-2283870f6986",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "50ffd6ef-6fbf-4086-99cc-b78b2d08a2fc",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "511ee7f5-b276-4c84-9ca7-7d48d6fe74ed",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "52968ae4-ea4c-42e7-adcb-1855433219b2",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "5331d7d8-4e19-4315-a8f1-c18a56341f77",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "54614b1c-6300-4980-bced-ac53f0cb6b82",
      :instrument :mandolin,
      :key-of     :c}
     {:id         #uuid "56c132a3-8ce8-4abc-9fd3-77ac7cd5b5ef",
      :instrument :mandolin,
      :key-of     :c}
     {:id #uuid "573c0640-0ca6-419a-9a4c-e747093d1b5c",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "58d69a1e-1819-40f9-8c2a-1212542701b2",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "5bf6acaf-5bfb-48d7-bec3-dd9bef5e99b2",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "6115d6a3-45f9-4d94-ad9a-f1222741ccc0",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "62d1d394-4448-4a9a-9a8a-af05c5770f86",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "630ffcaf-e8c7-4487-9b16-db6d5817d025",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "673b28ed-b5ce-41bd-b001-d922b3ab84c8",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "68d2a58c-c85a-4df7-acd0-12bc068ca091",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "68d38ae9-5d1d-4279-841a-5128ffdd9e31",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "6bae2685-5881-4c4c-a1b9-2e00cd54b097",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "73d323b0-bda2-4f9e-8bbe-b7313d0c7ec2",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "73daf65e-5cb0-4c1e-a0b2-f63ff95d7069",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "7868e306-dccc-4078-816f-b1b65e5cc6c8",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "7989f7ee-0eab-4acb-9805-389e3f31a865",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "79b9b30c-a9a7-4ae5-8e8a-f9f83b311a93",
      :instrument :mandolin,
      :key-of :c}
     {:id #uuid "7b95d0bd-6999-4a32-bd10-92172caa3ed4",
      :instrument :mandolin,
      :key-of :c}))))
