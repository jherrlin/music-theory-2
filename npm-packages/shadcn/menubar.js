function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
import * as React from "react";
import * as MenubarPrimitive from "@radix-ui/react-menubar";
import { Check, ChevronRight, Circle } from "lucide-react";
import { cn } from "shadcn/utils";
const MenubarMenu = MenubarPrimitive.Menu;
const MenubarGroup = MenubarPrimitive.Group;
const MenubarPortal = MenubarPrimitive.Portal;
const MenubarSub = MenubarPrimitive.Sub;
const MenubarRadioGroup = MenubarPrimitive.RadioGroup;
const Menubar = /*#__PURE__*/React.forwardRef(({
  className,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.Root, _extends({
  ref: ref,
  className: cn("flex h-10 items-center space-x-1 rounded-md border bg-background p-1", className)
}, props)));
Menubar.displayName = MenubarPrimitive.Root.displayName;
const MenubarTrigger = /*#__PURE__*/React.forwardRef(({
  className,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.Trigger, _extends({
  ref: ref,
  className: cn("flex cursor-default select-none items-center rounded-sm px-3 py-1.5 text-sm font-medium outline-none focus:bg-accent focus:text-accent-foreground data-[state=open]:bg-accent data-[state=open]:text-accent-foreground", className)
}, props)));
MenubarTrigger.displayName = MenubarPrimitive.Trigger.displayName;
const MenubarSubTrigger = /*#__PURE__*/React.forwardRef(({
  className,
  inset,
  children,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.SubTrigger, _extends({
  ref: ref,
  className: cn("flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[state=open]:bg-accent data-[state=open]:text-accent-foreground", inset && "pl-8", className)
}, props), children, /*#__PURE__*/React.createElement(ChevronRight, {
  className: "ml-auto h-4 w-4"
})));
MenubarSubTrigger.displayName = MenubarPrimitive.SubTrigger.displayName;
const MenubarSubContent = /*#__PURE__*/React.forwardRef(({
  className,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.SubContent, _extends({
  ref: ref,
  className: cn("z-50 min-w-[8rem] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2", className)
}, props)));
MenubarSubContent.displayName = MenubarPrimitive.SubContent.displayName;
const MenubarContent = /*#__PURE__*/React.forwardRef(({
  className,
  align = "start",
  alignOffset = -4,
  sideOffset = 8,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.Portal, null, /*#__PURE__*/React.createElement(MenubarPrimitive.Content, _extends({
  ref: ref,
  align: align,
  alignOffset: alignOffset,
  sideOffset: sideOffset,
  className: cn("z-50 min-w-[12rem] overflow-hidden rounded-md border bg-popover p-1 text-popover-foreground shadow-md data-[state=open]:animate-in data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2", className)
}, props))));
MenubarContent.displayName = MenubarPrimitive.Content.displayName;
const MenubarItem = /*#__PURE__*/React.forwardRef(({
  className,
  inset,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.Item, _extends({
  ref: ref,
  className: cn("relative flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50", inset && "pl-8", className)
}, props)));
MenubarItem.displayName = MenubarPrimitive.Item.displayName;
const MenubarCheckboxItem = /*#__PURE__*/React.forwardRef(({
  className,
  children,
  checked,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.CheckboxItem, _extends({
  ref: ref,
  className: cn("relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50", className),
  checked: checked
}, props), /*#__PURE__*/React.createElement("span", {
  className: "absolute left-2 flex h-3.5 w-3.5 items-center justify-center"
}, /*#__PURE__*/React.createElement(MenubarPrimitive.ItemIndicator, null, /*#__PURE__*/React.createElement(Check, {
  className: "h-4 w-4"
}))), children));
MenubarCheckboxItem.displayName = MenubarPrimitive.CheckboxItem.displayName;
const MenubarRadioItem = /*#__PURE__*/React.forwardRef(({
  className,
  children,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.RadioItem, _extends({
  ref: ref,
  className: cn("relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none focus:bg-accent focus:text-accent-foreground data-[disabled]:pointer-events-none data-[disabled]:opacity-50", className)
}, props), /*#__PURE__*/React.createElement("span", {
  className: "absolute left-2 flex h-3.5 w-3.5 items-center justify-center"
}, /*#__PURE__*/React.createElement(MenubarPrimitive.ItemIndicator, null, /*#__PURE__*/React.createElement(Circle, {
  className: "h-2 w-2 fill-current"
}))), children));
MenubarRadioItem.displayName = MenubarPrimitive.RadioItem.displayName;
const MenubarLabel = /*#__PURE__*/React.forwardRef(({
  className,
  inset,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.Label, _extends({
  ref: ref,
  className: cn("px-2 py-1.5 text-sm font-semibold", inset && "pl-8", className)
}, props)));
MenubarLabel.displayName = MenubarPrimitive.Label.displayName;
const MenubarSeparator = /*#__PURE__*/React.forwardRef(({
  className,
  ...props
}, ref) => /*#__PURE__*/React.createElement(MenubarPrimitive.Separator, _extends({
  ref: ref,
  className: cn("-mx-1 my-1 h-px bg-muted", className)
}, props)));
MenubarSeparator.displayName = MenubarPrimitive.Separator.displayName;
const MenubarShortcut = ({
  className,
  ...props
}) => {
  return /*#__PURE__*/React.createElement("span", _extends({
    className: cn("ml-auto text-xs tracking-widest text-muted-foreground", className)
  }, props));
};
MenubarShortcut.displayname = "MenubarShortcut";
export { Menubar, MenubarMenu, MenubarTrigger, MenubarContent, MenubarItem, MenubarSeparator, MenubarLabel, MenubarCheckboxItem, MenubarRadioGroup, MenubarRadioItem, MenubarPortal, MenubarSubContent, MenubarSubTrigger, MenubarGroup, MenubarSub, MenubarShortcut };