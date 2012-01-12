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
   (javax.swing UIManager JPanel JLabel ImageIcon BoxLayout JEditorPane
                JSeparator SwingConstants Box JSplitPane)
   (java.awt Font Color GridLayout Dimension)
   (javax.swing.border LineBorder)))

(def *app-consumer-key* "")
(def *app-consumer-secret* "")
(def *user-access-token* "")
(def *user-access-token-secret* "")

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
    (.setBackground tweet-text (first @colors))
    (.setBackground tweet-name (first @colors))
    (.setBackground tweet-panel (Color. 255 255 255))
    (.add tweet-panel (Box/createRigidArea (Dimension. 10 0)))
    (.add tweet-panel image-label)
    (.add tweet-panel (Box/createRigidArea (Dimension. 10 0)))
    (.add text-panel tweet-name)
    (.add text-panel tweet-text)
    (.add tweet-panel text-panel)
    (swap! tweets conj tweet-panel)))

(def panel1 (JPanel.))
(def panel2 (JPanel.))
(def f (javax.swing.JFrame.))
(def p (JPanel.))
(def split-pane (JSplitPane. JSplitPane/HORIZONTAL_SPLIT panel1 panel2))


(def track "java")

(defn new-panel []
  (.removeAll panel1)
  (.removeAll panel2)
  (.setDividerLocation split-pane 0.5)
  (doall (map #(do (.add panel1 %)
                   (.add panel1 (JSeparator. SwingConstants/HORIZONTAL))) (take 9 @tweets)))
  (doall (map #(do (.add panel2 %)
                   (.add panel2 (JSeparator. SwingConstants/HORIZONTAL))) (take 9 (drop 9 @tweets)))))

(defn add-to-panel [tweet]
  (add-components tweet)
  (new-panel)
  (when (> (count @tweets) 18)
    (swap! tweets butlast))
  (swap! colors reverse)
  (.revalidate panel1)
  (.revalidate panel2)
  (.revalidate p))

(declare *custom-streaming-callback* restart-on-exception)

(defn restart-on-exception [response throwable]
  (statuses-filter :params {:track track}
                   :oauth-creds *creds*
                   :callbacks *custom-streaming-callback*))

(def ^:dynamic *custom-streaming-callback*
  (AsyncStreamingCallback. (comp add-to-panel (fn [res boas] (json/read-json (.toString boas))))
                           (fn [res boas] (swap! errors inc))
                           restart-on-exception))


(defn -main [& args]
  (do (UIManager/setLookAndFeel (UIManager/getSystemLookAndFeelClassName))
      (statuses-filter :params {:track track}
                       :oauth-creds *creds*
                       :callbacks *custom-streaming-callback*)
      (doto f
        (.setLayout (java.awt.FlowLayout.))
        (.setContentPane split-pane))
      (.setBackground split-pane Color/WHITE)
      (.setLayout panel1 (BoxLayout. panel1 BoxLayout/PAGE_AXIS))
      (.setLayout panel2 (BoxLayout. panel2 BoxLayout/PAGE_AXIS))
      (.setResizeWeight split-pane 0.0)
      (.setDividerLocation split-pane 0.5)
      (.setVisible panel1 true)
      (.setVisible panel2 true)
      (.setVisible split-pane true)
      (.setVisible f true)))