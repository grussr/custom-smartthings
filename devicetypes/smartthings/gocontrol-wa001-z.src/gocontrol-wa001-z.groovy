import groovy.json.JsonOutput
/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "GoControl WA001-Z", namespace: "smartthings", author: "SmartThings", runLocally: true, minHubCoreVersion: '000.017.0012', executeCommandsLocally: false, ocfDeviceType: "x.com.st.d.remotecontroller", mnmn: "SmartThings", vid: "generic-4-button", mcdSync: true) {
		capability "Actuator"
		capability "Button"
		capability "Holdable Button"
		capability "Configuration"
		capability "Sensor"
		capability "Battery"
		capability "Health Check"
        
		fingerprint deviceId:"0x1801", inClusters:"0x5E, 0x86, 0x72, 0x5B, 0x85, 0x59, 0x73, 0x70, 0x80, 0x84, 0x5A, 0x7A", outClusters:"0x5B, 0x20", deviceJoinName: "GoControl WA001-Z"
	}

	simulator {
		status "button 1 pushed":  "command: 2001, payload: 01"
		status "button 1 held":  "command: 2001, payload: 15"
		status "button 2 pushed":  "command: 2001, payload: 29"
		status "button 2 held":  "command: 2001, payload: 3D"
		status "wakeup":  "command: 8407, payload: "
	}
	tiles {

		multiAttributeTile(name: "rich-control", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.button", key: "PRIMARY_CONTROL") {
				attributeState "default", label: ' ', action: "", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
			}
		}
		standardTile("battery", "device.battery", inactiveLabel: false, width: 6, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
		childDeviceTiles("outlets")
	}

}


def parse(String description) {
	def results = []
    log.debug "Parsing ${description}"
	if (!device.currentState("supportedButtonValues")) {
		sendEvent(name: "supportedButtonValues", value: JsonOutput.toJson(["pushed", "held"]), displayed: false)

		if (childDevices) {
			childDevices.each {
				it.sendEvent(name: "supportedButtonValues", value: JsonOutput.toJson(["pushed", "held"]), displayed: false)
			}
		}
	}

	if (description.startsWith("Err")) {
		results = createEvent(descriptionText:description, displayed:true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) results += zwaveEvent(cmd)
		if (!results) results = [ descriptionText: cmd, displayed: false ]
	}

	// log.debug("Parsed '$description' to $results")
	return results
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
	def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]

	def prevBattery = device.currentState("battery")
	if (!prevBattery || (new Date().time - prevBattery.date.time)/60000 >= 60 * 53) {
		results << response(zwave.batteryV1.batteryGet().format())
		result << response("delay 1200")  // leave time for device to respond to batteryGet
	}
	results += configurationCmds().collect{ response(it) }
	results << response(zwave.wakeUpV1.wakeUpNoMoreInformation().format())

	return results
}

def buttonEvent(button, held) {
	button = button as Integer
	def child
	Integer buttons

	if (device.currentState("numberOfButtons")) {
		buttons = (device.currentState("numberOfButtons").value).toBigInteger()
	} else {
		def zwMap = getZwaveInfo()
		buttons = 2 // Default for Key Fob

		sendEvent(name: "numberOfButtons", value: buttons, displayed: false)
	}

		String childDni = "${device.deviceNetworkId}/${button}"
		child = childDevices.find{it.deviceNetworkId == childDni}
		if (!child) {
			log.error "Child device $childDni not found"
		}

	if (held) {
			child?.sendEvent(name: "button", value: "held", data: [buttonNumber: 1], descriptionText: "$child.displayName was held", isStateChange: true)
		createEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
	} else {
			child?.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "$child.displayName was pushed", isStateChange: true)
		createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	Integer button = ((cmd.sceneId + 1) / 2) as Integer
	Boolean held = !(cmd.sceneId % 2)
	buttonEvent(button, held)
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd) {
    log.debug "---Central Scene Command--- ${device.displayName} sent ${cmd}"
    Integer button = cmd.sceneNumber
    Boolean held = (cmd.keyAttributes == 2)
    buttonEvent(button, held)
    
    /*if(cmd.keyAttributes == 0){
    	
         if(cmd.sceneNumber == 1) {
          
          	if(delayTime == true) {
          		runIn(2, pressup) }
		 	else if(delayTime == false) {
            createEvent(name: "button", value: "up", data: [buttonNumber: 1], descriptionText: "$device.displayName button up was pushed", isStateChange: true) }
        }
         else if(cmd.sceneNumber == 2) {
         	if(delayTime == true) {
            	runIn(2, pressdown) }
            else if(delayTime == false) {
          createEvent(name: "button", value: "down", data: [buttonNumber: 1], descriptionText: "$device.displayName button down was pushed", isStateChange: true) } 
        } 
        
    } else if(cmd.keyAttributes == 1){    
		createEvent(name: "button", value: "holdRelease", data: [buttonNumber: 1], descriptionText: "$device.displayName button was released", isStateChange: true)
    }
    
     else if(cmd.keyAttributes == 2){
		 if(cmd.sceneNumber == 1) {
          
          	if(delayTime == true) {
          		runIn(2, holdup) }
		 	else if(delayTime == false) {
            createEvent(name: "button", value: "up_hold", data: [buttonNumber: 1], descriptionText: "$device.displayName button up was held", isStateChange: true) }
        }
         else if(cmd.sceneNumber == 2) {
         	if(delayTime == true) {
            	runIn(2, holddown) }
            else if(delayTime == false) {
          createEvent(name: "button", value: "down_hold", data: [buttonNumber: 1], descriptionText: "$device.displayName button down was held", isStateChange: true) } 
        } 
	}*/
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [ name: "battery", unit: "%" ]

	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
	} else {
		map.value = cmd.batteryLevel
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	[ descriptionText: "$device.displayName: $cmd", linkText:device.displayName, displayed: false ]
}

def configurationCmds() {
	[ zwave.configurationV1.configurationSet(parameterNumber: 250, scaledConfigurationValue: 1).format(),
	  zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format() ]
}

def configure() {
	def cmd = configurationCmds()
	log.debug("Sending configuration: $cmd")
	return cmd
}


def installed() {
	initialize()
	Integer buttons = (device.currentState("numberOfButtons").value).toBigInteger()
	/*childDevices.each {
		try{
        	log.debug "Deleting child ${it.deviceNetworkId}"
			deleteChildDevice(it.deviceNetworkId)
		}
		catch (e) {
			log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
		}
	}*/
	if (!childDevices) { // Clicking "Update" from the Graph IDE calls installed(), so protect against trying to recreate children.
		log.debug "Creating Child devices"
        createChildDevices()
	}
}

def updated() {
	initialize()
	Integer buttons = (device.currentState("numberOfButtons").value).toBigInteger()
	sendEvent(name: "controlled", value: "scene", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 2, size: 1).format()))

		if (!childDevices) {
			createChildDevices()
		} else if (device.label != state.oldLabel) {
			childDevices.each {
				def segs = it.deviceNetworkId.split("/")
				def newLabel = "${device.displayName} button ${segs[-1]}"
				it.setLabel(newLabel)
			}
			state.oldLabel = device.label
		}

}

def initialize() {
	def results = []
	def buttons = 2

	sendEvent(name: "numberOfButtons", value: buttons, displayed: false)
	sendEvent(name: "supportedButtonValues", value: JsonOutput.toJson(["pushed", "held"]), displayed: false)
sendEvent(name: "DeviceWatch-Enroll", value: JsonOutput.toJson([protocol: "zwave", scheme:"untracked"]), displayed: false)
	results
}

private void createChildDevices() {
	state.oldLabel = device.label
	Integer buttons = (device.currentState("numberOfButtons").value).toBigInteger()

	for (i in 1..buttons) {
		def child = addChildDevice("Child Button",
				"${device.deviceNetworkId}/${i}",
				device.hubId,
				[completedSetup: true,
				 label: "${device.displayName} button ${i}",
				 isComponent: true,
				 componentName: "button$i",
				 componentLabel: "Button $i"])

		child.sendEvent(name: "supportedButtonValues", value: JsonOutput.toJson(["pushed", "held"]), displayed: false)
		child.sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], displayed: false)
	}
}