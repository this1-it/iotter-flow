what is com.vaadin.flow.server.VaadinServiceInitListener ?                                                                           
                                                                                                                                       
● VaadinServiceInitListener is a Vaadin Flow interface that allows you to hook into the Vaadin service initialization lifecycle.       
                                                                                                                                       
  Purpose: It's called once when the Vaadin application starts, letting you configure the VaadinService before any UIs are created.    
                                                                                                                                       
  Common uses:                                                                                                                         
  - Add UIInitListener to run code when each new UI (browser tab) is created                                                           
  - Add SessionInitListener for session setup                                                                                          
  - Configure request handlers, error handlers, etc.                                                                                   
                                                                                                                                       
  In our case:                                                                                                                         
  @Override                                                                                                                            
  public void serviceInit(ServiceInitEvent event) {                                                                                    
      // Called once at app startup                                                                                                    
      event.getSource().addUIInitListener(uiInitEvent -> {                                                                             
          // Called for EACH new UI (browser tab)                                                                                      
          UI ui = uiInitEvent.getUI();                                                                                                 
          UIEventBus eventBus = applicationContext.getBean(UIEventBus.class);                                                          
          registry.register(ui, eventBus);                                                                                             
          // ...                                                                                                                       
      });                                                                                                                              
  }                                                                                                                                    
                                                                                                                                       
  Registration: The META-INF/services/com.vaadin.flow.server.VaadinServiceInitListener file tells Java's ServiceLoader to discover and 
  instantiate our listener automatically.                                                                                              
                                                                                                                                       
  Alternative: In Vaadin with Spring, you can also use @SpringComponent and Spring will auto-detect it. The service file is a fallback 
  for non-Spring detection.                                                                                                            
