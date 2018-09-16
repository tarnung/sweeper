(ns sweeper.events
  (:require
    [re-frame.core :as rf]
    [sweeper.db :as db]
    [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
    [sweeper.logic :as logic]
    ))

(rf/reg-event-db
  ::initialize-db
  (fn-traced [_ _]
             db/default-db))

(rf/reg-event-db
  ::start-game
  (fn [db [_ size bombs]]
    (assoc db
      :panel :game
      :size size
      :bombs bombs
      :board (logic/create-board size bombs)
      :progress (logic/create-progress size)
      :won false
      :lost false)))

(rf/reg-event-fx
  ::discover
  (fn [{:keys [db]} [_ index]]
    (let [new-db (-> db
                     (assoc-in [:progress index] true)
                     (assoc :lost (= 9 (get-in db [:board index]))))
          next-fields (logic/surrounding-fields-if-zero (:board new-db)
                                                   (:progress new-db)
                                                   (:size new-db)
                                                   index)]
      (cond-> {:db new-db}
              (not (empty? next-fields)) (assoc :dispatch-later [{:ms       200
                                                             :dispatch [::discover-multiple next-fields]}])))))

(rf/reg-event-fx
  ::discover-multiple
  (fn [{:keys [db]} [_ fields]]
    (let [is-field (into #{} fields)
          new-db (-> db
                     (update :progress (fn [progress]
                                         (into []
                                               (map-indexed #(if (is-field %1)
                                                               true
                                                               %2)
                                                            progress)))))
          next-fields (logic/surrounding-fields-multiple (:board new-db)
                                                    (:progress new-db)
                                                    (:size new-db)
                                                    fields)]
      (cond-> {:db new-db}
              (not (empty? next-fields)) (assoc :dispatch-later [{:ms       200
                                                             :dispatch [::discover-multiple next-fields]}])))))

(rf/reg-event-db
  ::reset
  (fn [_ _]
    db/default-db))
