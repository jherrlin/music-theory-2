(ns se.jherrlin.music-theory.webapp.components.pagination
  (:require
   ["shadcn/pagination" :refer [Pagination
                                PaginationContent
                                PaginationEllipsis
                                PaginationItem
                                PaginationLink
                                PaginationNext
                                PaginationPrevious]]))


(defn pagination []
  [:div {:class "container"}
   [:> Pagination
    [:> PaginationContent
     #_[:> PaginationItem
        [:> PaginationPrevious {:title ""}]]
     [:> PaginationItem
      [:> PaginationLink {:is-active true} "A"]]
     [:> PaginationItem
      [:> PaginationLink "A#"]]
     [:> PaginationItem
      [:> PaginationLink "B"]]
     [:> PaginationItem
      [:> PaginationLink "C"]]
     [:> PaginationItem
      [:> PaginationLink "C#"]]
     [:> PaginationItem
      [:> PaginationLink "D"]]
     [:> PaginationItem
      [:> PaginationLink "D#"]]
     [:> PaginationItem
      [:> PaginationLink "E"]]
     [:> PaginationItem
      [:> PaginationLink "F"]]
     [:> PaginationItem
      [:> PaginationLink "F#"]]
     [:> PaginationItem
      [:> PaginationLink "G"]]
     [:> PaginationItem
      [:> PaginationLink "G#"]]
     #_[:> PaginationItem
        [:> PaginationNext {:title ""}]]]]])
