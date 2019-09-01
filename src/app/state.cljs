(ns app.state
  (:require [reagent.core :refer [atom]]))

(def profile-fields
  [{:id "social-security-card"
    :label "Social Security Card"
    :description "Required for jobs, benefits and more"
    :default {:image "/media/social-security-card.jpg"}
    :expandable true
    :edit false
    :share true}
   {:id "drivers-license"
    :label "Drivers License"
    :description "Required for a job or bank account"
    :default {:image "/media/drivers-license.jpg"}
    :expandable true
    :share true}
   {:id "health-insurance-card"
    :label "Health Insurance Card"
    :description "Required for healthcare"
    :default {:image "/media/bic-card.png"}
    :expandable true
    :share true}
   {:id "passport"
    :label "Passport"
    :description "Required for ID and flying"
    :default {:image "/media/Passport_card.jpg"}
    :expandable true
    :share true}
   {:id "birth-certificate"
    :label "Birth Certificate"
    :description "Required for benefits and as identification"
    :default {:image "/media/birth-certificate.jpg"}
    :expandable true
    :edit false
    :share true}
   {:id "new item"
    :label "Custom"
    :description "You can add custom images"
    :expandable true
    :share true}])
