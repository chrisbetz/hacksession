(ns phonecat-re-frame.subs
  (:require [re-frame.core :refer [register-sub, subscribe]])
  (:require-macros [reagent.ratom :refer [reaction]]))

(register-sub                                      ;; a new subscription handler
  :phones                                                   ;; usage (subscribe [:phones])
  (fn [db]
    ;; extracts the phones property from the db
    (reaction (:phones @db))))                              ;; pulls out :phones

(register-sub
  :search-input
  (fn [db]
    (reaction (:search-input @db))))

(register-sub                                      ;; a new subscription handler
  :phones                                                   ;; usage (subscribe [:phones])
  (fn [db]
    (reaction (:phones @db))))                              ;; pulls out :phones

(register-sub
  :selected-image-url
  (fn
    ;; extract the selected-image-url from the db. If it's not set return the first image of the current phone under query
    [db [_ phone-id]]
    (let [phone (subscribe [:phone-query phone-id])
          phone-details (subscribe [:phone-details])
          images (reaction (:images @phone))]
      ;; Note how we are sequencing reactions above. Whenever the phone ratom changes the images ratom will change as well
      (reaction
        (if @phone-details
          (if-let [image-url (:selected-image-url @phone-details)]
            image-url
            (first @images)))))))

(register-sub
  :phone-details
  (fn [db]
    (reaction (:phone-details @db))))

(register-sub
  :order-prop
  (fn [db]
    (reaction (:order-prop @db))))

(register-sub
  :phone-details
  (fn [db]
    (reaction (:phone-details @db))))

(register-sub
  :phone-query
  (fn
    ;; get info on the given phone id from the phone-details map
    [db [_ phone-id]]
    (let [phone-details-reaction (reaction (:phone-details @db))]
      (reaction ((keyword phone-id) @phone-details-reaction)))))
