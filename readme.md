Flowable Event Registry Demo
============================

Some demo code on how to use the event registry in flowable 6.5.0.

**note**
  I'm no expert -- I just used the docs and the source code to find out how
  to use this.
  
Prerequisites
-------------

- A RabbitMQ installation on localhost using the default (insecure!) credentials.

Example Use Case
----------------

Let's assume we have some system which sends messages to RabbitMQ which are JSON encoded and
looks like so:

    {
      "eventKeyValue": "myEvent",
      "customerName": "nexiles",
      "amount": 42
    }  

We want flowable to look for **events -- e.g. messages** of this kind and start a business process for each
message / event.

Testing
-------

- make sure RabbitMQ is running
- Start the application
- Using the RabbitMQ UI:
    - navigate to the "flowable-inbound" queue
    - publish a message - **!!make sure that you set "content_type" to "application/json" in the "properties"!!**
    
You should see something like this:


    2020-03-18 19:36:24.454  INFO 58827 --- [           main] c.n.e.f.FlowableEventRegistryApplication : Application started.
    2020-03-18 19:36:24.455  INFO 58827 --- [           main] c.n.e.f.FlowableEventRegistryApplication : Processes deployed: 1
    2020-03-18 19:36:24.456  INFO 58827 --- [           main] c.n.e.f.FlowableEventRegistryApplication : Events deployed:    1
    2020-03-18 19:37:00.726  INFO 58827 --- [         task-1] c.n.e.flowableeventregistry.Logger       : LOG: Process eventTest:1:5eed0edf-6947-11ea-a2e5-6ab7c11ecb8d activity theTask: nexiles
    2020-03-18 19:37:00.727 DEBUG 58827 --- [         task-1] c.n.e.flowableeventregistry.Logger       : LOG: var amount := 42
    2020-03-18 19:37:00.727 DEBUG 58827 --- [         task-1] c.n.e.flowableeventregistry.Logger       : LOG: var customerName := nexiles
    2020-03-18 19:37:02.675  INFO 58827 --- [         task-2] c.n.e.flowableeventregistry.Logger       : LOG: Process eventTest:1:5eed0edf-6947-11ea-a2e5-6ab7c11ecb8d activity theTask: nexiles
    2020-03-18 19:37:02.675 DEBUG 58827 --- [         task-2] c.n.e.flowableeventregistry.Logger       : LOG: var amount := 42
    2020-03-18 19:37:02.675 DEBUG 58827 --- [         task-2] c.n.e.flowableeventregistry.Logger       : LOG: var customerName := nexiles
    2020-03-18 19:37:03.260  INFO 58827 --- [         task-3] c.n.e.flowableeventregistry.Logger       : LOG: Process eventTest:1:5eed0edf-6947-11ea-a2e5-6ab7c11ecb8d activity theTask: nexiles
    2020-03-18 19:37:03.260 DEBUG 58827 --- [         task-3] c.n.e.flowableeventregistry.Logger       : LOG: var amount := 42
    2020-03-18 19:37:03.260 DEBUG 58827 --- [         task-3] c.n.e.flowableeventregistry.Logger       : LOG: var customerName := nexiles


What does the code do
---------------------

- Define a **inbound channel**  -- `inbound.channel` in resources/eventregistry
- Define the **event** -- `event-one.event` in resources/eventregistry
- Define a **process** -- `event-test-process.bpmn2ß.xml` in resources/processes

### inbound channel

Defines a channel where flowable listens for events.

- It's a "inbound" channel -- messages flow from the queue to flowable
- We use rabbitmq
- Our messages are JSON
- The messages we receive have a JSON Field "eventKeyValue" which defines the event type.  See above,
  the event type is "myEvent" for the example message.
- We want flowable to bind to the queue "flowable-inbound".  This is a queue which must exist in RabbitMQ and
  our messages must be routed to this queue.

    {
      "key": "testChannel",
      "category": "channel",
      "name": "Test channel",
      "description": "test Inbound Channel",
      "channelType": "inbound",
      "type": "rabbit",
      "deserializerType": "json",
      "channelEventKeyDetection": {
        "jsonField": "eventKeyValue"
      },
      "queues": ["flowable-inbound"]
}

### Event Definition

This defines the shape of the message and a binding to the inbound channel.  I don't yet know
what the other stuff means :)

    {
      "key": "myEvent",
      "name": "My event",
      "inboundChannelKeys": [
        "test-channel"
      ],
      "correlationParameters": [
        {
          "name": "customerId",
          "type": "string"
        }
      ],
      "payload": [
        {
          "name": "customerName",
          "type": "string"
        },
        {
          "name": "foo",
          "type": "string"
        },
        {
          "name": "amount",
          "type": "integer"
        }
      ]
    }
### Process

So finally we define a BPMN process.

- The **startEvent** refers to our event type `myEvent`
- We define two mappings from event payload to business process variable:
  - customerName -> customerName
  - amount -> amount
- The script task uses the Logger bean to log a message.


    <?xml version="1.0" encoding="UTF-8"?>
    <definitions
            xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
            xmlns:flowable="http://flowable.org/bpmn"
            targetNamespace="Examples">

        <process id="eventTest" name="Process to test events">
            <startEvent id="theStart" >
                <extensionElements>
                    <flowable:eventType xmlns:flowable="http://flowable.org/bpmn">myEvent</flowable:eventType>
                    <flowable:eventOutParameter xmlns:flowable="http://flowable.org/bpmn"
                                                source="customerName"
                                                sourceType="string"
                                                target="customerName"/>
                    <flowable:eventOutParameter xmlns:flowable="http://flowable.org/bpmn"
                                                source="amount"
                                                sourceType="integer"
                                                target="amount"/>
                </extensionElements>
            </startEvent>
            <sequenceFlow id="flow1" sourceRef="theStart" targetRef="theTask" />
            <serviceTask id="theTask" name="my task" flowable:expression="#{Logger.log(execution, customerName)}" />
            <sequenceFlow id="flow2" sourceRef="theTask" targetRef="theEnd" />
            <endEvent id="theEnd" />
        </process>

    </definitions>


