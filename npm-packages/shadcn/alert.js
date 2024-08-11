function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
import * as React from "react";
import { cva } from "class-variance-authority";
import { cn } from "shadcn/utils";
const alertVariants = cva("relative w-full rounded-lg border p-4 [&>svg~*]:pl-7 [&>svg+div]:translate-y-[-3px] [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-4 [&>svg]:text-foreground", {
  variants: {
    variant: {
      default: "bg-background text-foreground",
      destructive: "border-destructive/50 text-destructive dark:border-destructive [&>svg]:text-destructive"
    }
  },
  defaultVariants: {
    variant: "default"
  }
});
const Alert = /*#__PURE__*/React.forwardRef(({
  className,
  variant,
  ...props
}, ref) => /*#__PURE__*/React.createElement("div", _extends({
  ref: ref,
  role: "alert",
  className: cn(alertVariants({
    variant
  }), className)
}, props)));
Alert.displayName = "Alert";
const AlertTitle = /*#__PURE__*/React.forwardRef(({
  className,
  ...props
}, ref) => /*#__PURE__*/React.createElement("h5", _extends({
  ref: ref,
  className: cn("mb-1 font-medium leading-none tracking-tight", className)
}, props)));
AlertTitle.displayName = "AlertTitle";
const AlertDescription = /*#__PURE__*/React.forwardRef(({
  className,
  ...props
}, ref) => /*#__PURE__*/React.createElement("div", _extends({
  ref: ref,
  className: cn("text-sm [&_p]:leading-relaxed", className)
}, props)));
AlertDescription.displayName = "AlertDescription";
export { Alert, AlertTitle, AlertDescription };