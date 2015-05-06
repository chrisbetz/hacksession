(ns phonecat-re-frame.handlers
  (:require [ajax.core :refer [GET]]
            [re-frame.core :refer [register-handler, dispatch]]
            [re-frame.middleware :refer [undoable]]))

(register-handler
  :set-image
  (undoable "Changed picture")
  (fn
    ;; take an image url and set it in the db
    [app-state [_ selected-image-url]]
    (assoc-in app-state [:phone-details :selected-image-url] selected-image-url)))

(register-handler
  :process-phones-response
  (fn
    ;; store the response of fetching the phones list in the phones attribute of the db
    [app-state [_ response]]
    (assoc-in app-state [:phones] response)))

(register-handler
  :process-phones-bad-response
  (fn
    ;; log a bad response fetching the phones list
    [app-state [_ response]]
    (println "Error getting phone list response")
    (println response)
    app-state))

(register-handler
  :load-phones
  (fn
    ;; Fetch the list of phones and process the response
    [app-state _]
    (GET "phones/phones.json"
         {:handler         #(dispatch [:process-phones-response %1])
          :error-handler   #(dispatch [:process-phones-bad-response %1])
          :response-format :json
          :keywords?       true})
    app-state))

(register-handler
  :load-phone-detail
  (fn
    ;; fetch information for the phone with the given phone-id
    [app-state [_ phone-id]]
    (GET (str "phones/" phone-id ".json")
         {:handler         #(dispatch [:process-phone-detail-response phone-id %1])
          :error-handler   #(dispatch [:process-phone-detail-bad-response phone-id %1])
          :response-format :json
          :keywords?       true})
    app-state))

(register-handler
  :process-phone-detail-response
  (fn
    ;; store info for the specific phone-id in the db
    [app-state [_ phone-id response]]
    (assoc-in app-state [:phone-details (keyword phone-id)] response)))

(register-handler
  :process-phone-detail-bad-response
  (fn
    [app-state [_ [phone-id response]]]
    (println "Error getting phone detail for id: " phone-id)
    (println response)
    app-state))

(register-handler
  :initialise-db                                            ;; usage: (dispatch [:initialise-db])
  (fn
    [_ _]                                                   ;; Ignore both params (db and v).
    {:phones        []
     :phone-details {}
     :search-input  ""
     :order-prop    "name"}))

(defn handle-search-input-entered
  [app-state [_ search-input]]
  (assoc-in app-state [:search-input] search-input))

(defn handle-order-prop-changed
  [app-state [_ order-prop]]
  (assoc-in app-state [:order-prop] order-prop))

(register-handler
  :search-input-entered
  handle-search-input-entered)

(register-handler
  :order-prop-changed
  handle-order-prop-changed)