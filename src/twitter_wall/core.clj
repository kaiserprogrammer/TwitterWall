(ns twitter-wall.core
  (:gen-class)
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.streaming])
  (:require
   [clojure.data.json :as json])
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)
   (javax.swing UIManager)))

(def *app-consumer-key* "jzl7xHs9svVflj8GG8pyFg")
(def *app-consumer-secret* "n98PLQlc8TGdzaJF2IG0DM308UJhq6A08YciZEnUjU")
(def *user-access-token* "88661852-PVkKrSCc0l0U6i3gIyRcLuzcD0vEt6zOLAnIebQqQ")
(def *user-access-token-secret* "ylP6OPKi25eHyCHmeltP6wClOn2wS2qdr3tLVdH0LF8")

(def ^:dynamic *creds*
  (make-oauth-creds
   *app-consumer-key*
   *app-consumer-secret*
   *user-access-token*
   *user-access-token-secret*))

(def tweets (atom '()))

(def errors (atom 0))

(defn add-components [tweet]
  (swap! tweets conj (javax.swing.JLabel. (:text tweet) (javax.swing.ImageIcon. (java.net.URL. (:profile_image_url (:user tweet)))) javax.swing.JLabel/RIGHT)))

(def panel (javax.swing.JPanel.))

(defn new-panel []
  (.removeAll panel)
  (doall (map #(.add panel %) @tweets)))

(defn add-to-panel [tweet]
  (add-components tweet)
  (new-panel)
  (.revalidate panel))

(def ^:dynamic *custom-streaming-callback*
  (AsyncStreamingCallback. (comp add-to-panel (fn [res boas] (json/read-json (.toString boas))))
                           (fn [res boas] (swap! errors inc))
                           exception-print))


(defn -main [& args]
  (do (UIManager/setLookAndFeel (UIManager/getSystemLookAndFeelClassName))
      (statuses-filter :params {:track "java"}
                       :oauth-creds *creds*
                       :callbacks *custom-streaming-callback*)
      (let [f (javax.swing.JFrame.)]
        (doto f
          (.setLayout (java.awt.GridLayout.))
          (.setContentPane panel)
          (.setVisible true)))))