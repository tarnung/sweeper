(ns sweeper.subs
  (:require
    [re-frame.core :as rf]
    [sweeper.logic :as logic]))

(rf/reg-sub
  ::panel
  (fn [db]
    (:panel db)))

(rf/reg-sub
  ::board
  (fn [db]
    (:board db)))

(rf/reg-sub
  ::progress
  (fn [db]
    (:progress db)))

(rf/reg-sub
  ::won
  (fn [db]
    (and (not (:lost db))
         (= (:bombs db)
            (count (filter false? (:progress db)))))))

(rf/reg-sub
  ::lost
  (fn [db]
    (:lost db)))

(rf/reg-sub
  ::size
  (fn [db]
    (:size db)))

(rf/reg-sub
  ::disarmed
  (fn [db [_ index]]
    (logic/disarmed? (:board db)
                     (:progress db)
                     (:size db)
                     index)))