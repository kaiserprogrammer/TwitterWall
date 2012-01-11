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
   (javax.swing UIManager JPanel JLabel ImageIcon BoxLayout JEditorPane JSeparator SwingConstants Box )
   (java.awt Font Color GridLayout Dimension)
   (javax.swing.border LineBorder)))

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

(def colors (atom [(Color. 255 255 255) (Color. 240 240 240)]))

(defn add-components [tweet]
  (let [tweet-panel (JPanel.)
        image (ImageIcon. (java.net.URL. (:profile_image_url (:user tweet))))
        image-label (JLabel. image)
        text-panel (JPanel.)
        tweet-text (JEditorPane.)
        tweet-name (JEditorPane.)]
    (.setEditable tweet-text false)
    (.setEditable tweet-name false)
    (.setText tweet-text (:text tweet))
    (.setText tweet-name (:name (:user tweet)))
    (.setLayout tweet-panel (BoxLayout. tweet-panel BoxLayout/X_AXIS))
    (.setLayout text-panel (BoxLayout. text-panel BoxLayout/Y_AXIS))
    (.setFont tweet-text (Font. "Serif" Font/PLAIN 18))
    (.setFont tweet-name (Font. "Inconsolata" Font/BOLD 20))
    ;; (.setOpaque label true)
    (.setBackground tweet-text (first @colors))
    (.setBackground tweet-name (first @colors))
    (.setBackground tweet-panel (Color. 255 255 255))
    (.add tweet-panel (Box/createRigidArea (Dimension. 10 0)))
    (.add tweet-panel image-label)
    (.add tweet-panel (Box/createRigidArea (Dimension. 10 0)))
    (.add text-panel tweet-name)
    (.add text-panel tweet-text)
    (.add tweet-panel text-panel)
    ;; (.setBorder tweet-panel (LineBorder. Color/WHITE 5))
    ;; (print tweet)
    (swap! tweets conj tweet-panel)))

(def panel (JPanel.))

(defn new-panel []
  (.removeAll panel)
  (doall (map #(do (.add panel %)
                   (.add panel (JSeparator. SwingConstants/HORIZONTAL))) @tweets)))

(defn add-to-panel [tweet]
  (add-components tweet)
  (new-panel)
  (when (> (count @tweets) 20)
    (swap! tweets butlast))
  (swap! colors reverse)
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
          (.setLayout (java.awt.FlowLayout.))
          (.setContentPane panel)
          (.setVisible true))
        ;; (.setLayout panel (GridLayout. 2 2))
        (.setLayout panel (BoxLayout. panel BoxLayout/PAGE_AXIS))
        (.setBackground panel Color/WHITE))))