(ns sweeper.logic)

(defn surrounding-fields [size index]
  (let [upper-edge (< index size)
        right-edge (= (dec size) (mod index size))
        lower-edge (>= index (* size (dec size)))
        left-edge (= 0 (mod index size))]
    (cond-> []
            (and (not left-edge)
                 (not upper-edge)) (conj (dec (- index size)))
            (not upper-edge) (conj (- index size))
            (and (not upper-edge)
                 (not right-edge)) (conj (inc (- index size)))
            (not right-edge) (conj (inc index))
            (and (not right-edge)
                 (not lower-edge)) (conj (inc (+ index size)))
            (not lower-edge) (conj (+ index size))
            (and (not lower-edge)
                 (not left-edge)) (conj (dec (+ index size)))
            (not left-edge) (conj (dec index)))))

(defn nr-of-adjacent-bombs [board-with-bombs size index]
  (let [adjacent-indexes (surrounding-fields size index)
        adjacent-contents (map board-with-bombs adjacent-indexes)
        nr-of-bombs (count (filter #(= 9 %) adjacent-contents))]
    nr-of-bombs))

(defn create-board [size bombs]
  (let [empty-board (repeat (- (* size size)
                               bombs) 
                            0)
        board-with-bombs (shuffle (apply conj empty-board (repeat bombs 9)))
        board-with-heatmap (into []
                                 (map-indexed #(if (= 9 %2)
                                                 9
                                                 (nr-of-adjacent-bombs board-with-bombs size %1))
                                              board-with-bombs))]
    board-with-heatmap))

(defn create-progress [size]
  (into []
        (repeat (* size size) false)))

(defn surrounding-fields-if-zero [board progress size index]
  (if (zero? (board index))
    (filter #(not (progress %))
      (surrounding-fields size index))
    []))

(defn surrounding-fields-multiple [board progress size fields]
  (distinct (mapcat #(surrounding-fields-if-zero board progress size %) fields)))

(defn disarmed? [board progress size index]
  (if (= 9 (board index))
    (let [fields (surrounding-fields size index)]
      (every? #(or (progress %)
                   (= 9 (board %)))
              fields))
    false))

#_(defn make-progress [board progress size index]
  (loop [steps [[index]]
         current-progress (assoc progress index true)]
    (let [touched-fields (into #{} (flatten steps))
          last-steps (last steps)
          surrounding-zeroes (into []
                                   (distinct (filter #(and (= 0 (board %))
                                                           (not (touched-fields %)))
                                                     (mapcat #(surrounding-fields size %)
                                                             last-steps))))]
      (if (empty? surrounding-zeroes)
        steps
        (recur (conj steps surrounding-zeroes)
               (let [touched? (into #{} surrounding-zeroes)]
                 (map-indexed #((if (touched? %1)
                                  true
                                  %2))
                              current-progress)))))))

#_(disarmed? @(re-frame.core/subscribe [:sweeper.subs/board]) @(re-frame.core/subscribe [:sweeper.subs/progress]) 10 15)