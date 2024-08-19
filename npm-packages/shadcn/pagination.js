function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
import * as React from "react";
import { ChevronLeft, ChevronRight, MoreHorizontal } from "lucide-react";
import { cn } from "shadcn/utils";
import { buttonVariants } from "shadcn/button";
const Pagination = ({
  className,
  ...props
}) => /*#__PURE__*/React.createElement("nav", _extends({
  role: "navigation",
  "aria-label": "pagination",
  className: cn("mx-auto flex w-full justify-center", className)
}, props));
Pagination.displayName = "Pagination";
const PaginationContent = /*#__PURE__*/React.forwardRef(({
  className,
  ...props
}, ref) => /*#__PURE__*/React.createElement("ul", _extends({
  ref: ref,
  className: cn("flex flex-row items-center gap-1", className)
}, props)));
PaginationContent.displayName = "PaginationContent";
const PaginationItem = /*#__PURE__*/React.forwardRef(({
  className,
  ...props
}, ref) => /*#__PURE__*/React.createElement("li", _extends({
  ref: ref,
  className: cn("", className)
}, props)));
PaginationItem.displayName = "PaginationItem";
const PaginationLink = ({
  className,
  isActive,
  size = "icon",
  ...props
}) => /*#__PURE__*/React.createElement("a", _extends({
  "aria-current": isActive ? "page" : undefined,
  className: cn(buttonVariants({
    variant: isActive ? "outline" : "ghost",
    size
  }), className)
}, props));
PaginationLink.displayName = "PaginationLink";
const PaginationPrevious = ({
  className,
  ...props
}) => /*#__PURE__*/React.createElement(PaginationLink, _extends({
  "aria-label": "Go to previous page",
  size: "default",
  className: cn("gap-1 pl-2.5", className)
}, props), /*#__PURE__*/React.createElement(ChevronLeft, {
  className: "h-4 w-4"
}), /*#__PURE__*/React.createElement("span", null, "Previous"));
PaginationPrevious.displayName = "PaginationPrevious";
const PaginationNext = ({
  className,
  ...props
}) => /*#__PURE__*/React.createElement(PaginationLink, _extends({
  "aria-label": "Go to next page",
  size: "default",
  className: cn("gap-1 pr-2.5", className)
}, props), /*#__PURE__*/React.createElement("span", null, "Next"), /*#__PURE__*/React.createElement(ChevronRight, {
  className: "h-4 w-4"
}));
PaginationNext.displayName = "PaginationNext";
const PaginationEllipsis = ({
  className,
  ...props
}) => /*#__PURE__*/React.createElement("span", _extends({
  "aria-hidden": true,
  className: cn("flex h-9 w-9 items-center justify-center", className)
}, props), /*#__PURE__*/React.createElement(MoreHorizontal, {
  className: "h-4 w-4"
}), /*#__PURE__*/React.createElement("span", {
  className: "sr-only"
}, "More pages"));
PaginationEllipsis.displayName = "PaginationEllipsis";
export { Pagination, PaginationContent, PaginationEllipsis, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious };