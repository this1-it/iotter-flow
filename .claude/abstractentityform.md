 The side-drawer-content div has none of its Java-set styles applied — display: block (not flex), overflow: visible (not auto), flexGrow: 0, and style:
   null. The Java code in SideDrawer sets flex styles on it, but they're not reaching the DOM.                                                        
                                                                                                                                                        
  This means the @CssImport for side-drawer-global.css is also not targeting the right elements, and the SideDrawer.setDrawerContent() inline styles are
   being lost.

  The core issue: The side-drawer-content div is being rendered OUTSIDE the shadow DOM of vaadin-dialog-overlay in a way that the Java inline styles set
   before dialog.open() are not propagated to the client.

  The fix has two parts:
  1. The side-drawer-content div needs its styles applied — the fact they're not showing means the component might be a different instance in the DOM
  2. The mainLayout in ModbusProfileForm.getFieldsLayout() returns a new VerticalLayout each call, and createContent() should be setting
  height:0/flex-grow:1 on it — but these too are missing
