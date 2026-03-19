 DevicesListing lives inside a View (route target) whose parent chain leads to the viewport — every ancestor has a defined pixel height. height: 100% resolves  
  correctly all the way down.
                                                                                                                                                                 
  ChannelListing lives inside a TabSheet tab inside a DeviceForm inside a SideDrawer dialog. That parent chain has no defined pixel height on the TabSheet     
  content area. So:

  TabSheet content area       → height: auto (no pixel height)
    └─ Composite root VL      → height: 100% of auto = 0
         └─ mainLayout         → height: 100% of 0 = 0
              ├─ toolbar        → shows (intrinsic height from buttons, overflows)
              └─ content        → height: 100% of 0 = 0
                   └─ Grid      → height: 100% of 0 = 0  ← INVISIBLE
