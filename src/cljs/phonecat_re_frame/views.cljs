(ns phonecat-re-frame.views
  (:require phonecat-re-frame.subs
            phonecat-re-frame.handlers
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe, dispatch]]
            [re-com.core :refer [button]])
  (:require-macros [reagent.ratom :refer [reaction]]))

;; -------------------------
;; Phone List View

(defn phone-component
  "individual phone component in the phoens list view"
  [phone]
  [:li {:class "thumbnail phone-listing"}
   [:a {:href  (str "#/phones/" (:id phone))
        :class "thumb"}
    [:img {:src (:imageUrl phone)}]]
   [:a {:href (str "#/phones/" (:id phone))} (:name phone)]
   [:p (:snippet phone)]])

(defn matches-query?
  "checks if the search input matches a name or snippet of the given phone"
  [search-input phone]
  (or
    (= "" search-input)
    (re-find (re-pattern search-input) (:name phone))
    (re-find (re-pattern search-input) (:snippet phone))))

(defn phones-component
  "component for the list of phones"
  []
  (let [phones (subscribe [:phones])
        search-input (subscribe [:search-input])
        order-prop (subscribe [:order-prop])]
    (fn []
      [:ul {:class "phones"}
       (for [phone (->> @phones
                        (filter (partial matches-query? @search-input))
                        (sort-by (keyword @order-prop)))]
         ^{:key (:name phone)} [phone-component phone])])))

(defn search-component
  "component for the search input"
  []
  (fn []
    [:div "Search"
     [:input {:on-change #(dispatch [:search-input-entered (-> % .-target .-value)])}]]))

(defn mark-selected
  "mark the given select element as selected if the order-prop matches the value of the element passed in"
  [props order-prop current-prop-value]
  (if (= order-prop current-prop-value)
    (reagent/merge-props props {:selected "selected"})
    props))

(defn order-by-component
  "component to define how you want to order the phones list"
  []
  (let [order-prop (subscribe [:order-prop])]
    (fn []
      [:div "Sort by: "
       [:select {:on-change #(dispatch [:order-prop-changed (-> % .-target .-value)])}
        [:option (mark-selected {:value "name"} @order-prop "name") "Alphabetical"]
        [:option (mark-selected {:value "age"} @order-prop "age") "Newest"]]])))

(defn home-page
  "defines the ome page which will be the phone list component"
  []
  [:div {:class "container-fluid"}
   [:div {:class "row"}
    [:div {:class "col-md-2"}
     [search-component]
     [order-by-component]]
    [:div {:class "col-md-10"}
     [phones-component]]]])

;; -------------------------
;; Phone details views

(defn phone-info-template
  "template for listing a set of phone attributes"
  [section-title attributes-map]
  [:li
   [:span section-title]
   [:dl
    (map (fn [attribute]
           ^{:key (:name attribute)} [:div
                                      [:dt (:name attribute)]
                                      [:dd (let [value (:value attribute)]
                                             (cond
                                               (true? value) "\u2713"
                                               (false? value) "\u2718"
                                               (vector? value) (clojure.string/join ", " value)
                                               :else value))]])
         attributes-map)]])

(defn thumbnails
  "component for displaying thumbnails of the phone"
  [phone]
  [:ul {:class "phone-thumbs"}
   (for [image (:images @phone)]
     ^{:key image} [:li [:img {:src      image
                               :class    "phone"
                               :on-click #(dispatch [:set-image image])}]])])

(defn undo-button
  "An undo button that de-selects the previously selected thumbnail"
  [undos?]
  [button
   :label "Undo Selection"
   :tooltip "I am not very useful"
   :tooltip-position :below-center
   :disabled? (not @undos?)
   :on-click #(dispatch [:undo])
   :class "btn-default"]
  )

(defn availability
  [availability]
  [:li
   [:span "Availability and Networks"]
   [:dl
    [:dt "Availability"]
    (for [availability @availability] availability)]])

(defn battery
  [battery]
  [phone-info-template "Battery" [{:name  "Type"
                                   :value (:type @battery)}
                                  {:name  "Talk Time"
                                   :value (:talkTime @battery)}
                                  {:name  "Standby time (max)"
                                   :value (:standbyTime @battery)}]])

(defn storage-and-memory
  [storage]
  [phone-info-template "Storage And Memory" [{:name  "RAM"
                                              :value (:ram @storage)}
                                             {:name  "Internal Storage"
                                              :value (:flash @storage)}]])

(defn connectivity
  [connectivity]
  [phone-info-template "Connectivity" [{:name  "Network Support"
                                        :value (:cell @connectivity)}
                                       {:name  "Wifi"
                                        :value (:wifi @connectivity)}
                                       {:name  "Bluetooth"
                                        :value (:bluetooth @connectivity)}]])

(defn android
  [android]
  [phone-info-template "Android" [{:name  "OS Version"
                                   :value (:os @android)}
                                  {:name  "UI"
                                   :value (:ui @android)}]])

(defn size-and-weight
  [size-and-weight]
  [phone-info-template "Size And Weight" [{:name  "Dimensions"
                                           :value (:dimensions @size-and-weight)}
                                          {:name  "Weight"
                                           :value (:weight @size-and-weight)}]])

(defn display
  [display]
  [phone-info-template "Display" [{:name  "Screen size"
                                   :value (:screenSize @display)}
                                  {:name  "Screen resolution"
                                   :value (:screenResolution @display)}
                                  {:name  "Touch screen"
                                   :value (:touchScreen @display)}]])

(defn hardware
  [hardware]
  [phone-info-template "Hardware" [{:name  "CPU"
                                    :value (:cpu @hardware)}
                                   {:name  "USB"
                                    :value (:usb @hardware)}
                                   {:name  "Audio / headphone jack"
                                    :value (:audioJack @hardware)}
                                   {:name  "FM Radio"
                                    :value (:fmRadio @hardware)}
                                   {:name  "Accelerometer"
                                    :value (:accelerometer @hardware)}]])

(defn camera
  [camera]
  [phone-info-template "Camera" [{:name  "Primary"
                                  :value (:primary @camera)}
                                 {:name  "Features"
                                  :value (:features @camera)}]])

(defn additional-features
  [additional-features]
  [:li
   [:span "Additional Features"]
   [:dd @additional-features]])

(defn specs
  "component for displaying the specs of the phone"
  [phone]
  [:ul {:class "specs"}
   [availability (reaction (:availability @phone))]
   [battery (reaction (:battery @phone))]
   [storage-and-memory (reaction (:storage @phone))]
   [connectivity (reaction (:connectivity @phone))]
   [android (reaction (:android @phone))]
   [display (reaction (:display @phone))]
   [hardware (reaction (:hardware @phone))]
   [camera (reaction (:camera @phone))]
   [size-and-weight (reaction (:sizeAndWeight @phone))]
   [additional-features (reaction (:additionalFeatures @phone))]])

(defn phone-page
  "top level component for the phone page"
  [{phone-id :phone-id}]
  (let [phone (subscribe [:phone-query phone-id])
        image-url (subscribe [:selected-image-url phone-id])
        undos? (subscribe [:undos?])]
    (fn []
      [:div
       [:img {:src   @image-url
              :class "phone"}]
       [:h1 (:name @phone)]
       [:p (:description @phone)]
       [thumbnails phone]
       [undo-button undos?]
       [specs phone]])))