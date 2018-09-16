(ns sweeper.views
  (:require
    [re-frame.core :as rf]
    [breaking-point.core :as bp]
    [sweeper.subs :as subs]
    [reagent.core :as r]))

(defn setup-panel []
  (let [size (r/atom 10)
        bombs (r/atom 10)]
    (fn []
      [:div
       [:h1 "SWEEPER"]
       [:form
        {:style {:display         "flex"
                 :flex-direction  "column"
                 :justify-content "center"}}
        [:label
         {:for "size"}
         "size: "]
        [:input
         {:type      "number"
          :id        "size"
          :value     @size
          :on-change #(reset! size (-> % .-target .-value js/parseInt ))
          }]
        [:label
         {:for "bombs"}
         "number of bombs: "]
        [:input
         {:type "number"
          :id   "bombs"
          :value @bombs
          :on-change #(reset! bombs (-> % .-target .-value js/parseInt ))}]
        [:input
         {:type     "submit"
          :value    "begin"
          :on-click (fn [e]
                      (.preventDefault e)
                      (rf/dispatch [:sweeper.events/start-game @size @bombs]))}]]])))

(defn game-field [row-nr field-nr field-content]
  (let [won @(rf/subscribe [:sweeper.subs/won])
        lost @(rf/subscribe [:sweeper.subs/lost])
        progress @(rf/subscribe [::subs/progress])
        size @(rf/subscribe [:sweeper.subs/size])
        index (+ field-nr (* size row-nr))
        discovered (progress index)
        disarmed @(rf/subscribe [:sweeper.subs/disarmed index])]
    [:div {:style    {:cursor           "pointer"
                      :width            20
                      :height           20
                      :background-color (if discovered
                                          "white"
                                          "gray")
                      :margin           1}
           :on-click #(if (and (not disarmed)
                               (not won)
                               (not lost)) (rf/dispatch [:sweeper.events/discover index]))}
     (if discovered (case field-content
                      9 "\uD83D\uDCA3"
                      field-content)
                    (if (and (= 9 field-content)
                             disarmed) "\uD83D\uDEA9"))]))

(defn game-row [row-nr row-content]
  [:div {:style {:display        "flex"
                 :flex-direction "row"}}
   (doall (for [[field-nr field-content] (map-indexed vector row-content)]
            ^{:key field-nr}
            [game-field row-nr field-nr field-content]))])

(defn game-board []
  (let [board @(rf/subscribe [:sweeper.subs/board])
        size @(rf/subscribe [:sweeper.subs/size])]
    [:div {:style {:display "flex"
                   :flex-direction "column"}}
     (doall (for [[row-nr row-content] (map-indexed vector (partition size board))]
              ^{:key row-nr}
              [game-row row-nr row-content]))]))

(defn game-panel []
  (let [won @(rf/subscribe [:sweeper.subs/won])
        lost @(rf/subscribe [:sweeper.subs/lost])]
    [:div
     [:h1 (cond won "you did it!"
                lost "boom!"
                :default "step carefully")]
     [game-board]
     [:button {:on-click #(rf/dispatch [:sweeper.events/reset])}
      "reset"]]))

(defn choose-panel [panel]
  ({:setup setup-panel
    :game game-panel}
    panel))

(defn main-panel []
  (let [panel (choose-panel @(rf/subscribe [:sweeper.subs/panel]))]
    [:div [panel]]))
