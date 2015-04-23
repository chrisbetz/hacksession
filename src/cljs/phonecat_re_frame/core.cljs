(ns phonecat-re-frame.core
    (:require phonecat-re-frame.subs
              phonecat-re-frame.handlers
              [phonecat-re-frame.views :refer [home-page, phone-page]]
              [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [re-frame.core :as re-frame])
    (:require-macros [reagent.ratom  :refer [reaction]])
    (:import goog.History))

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/phones" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/phones/:phone-id" {:as params}
  (session/put! :current-page #'phone-page)
  (session/put! :params params)
  (re-frame/dispatch [:load-phone-detail (:phone-id params)]))

(defn redirect-to
  [resource]
  (secretary/dispatch! resource)
  (.setToken (History.) resource))

(secretary/defroute "*" []
  (redirect-to "/phones"))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn current-page []
  [(session/get :current-page) (session/get :params)])

(defn init! []
  (hook-browser-navigation!)
  (re-frame/dispatch [:initialise-db])
  (re-frame/dispatch [:load-phones])
  (reagent/render-component [current-page] (.getElementById js/document "app")))
