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
 *  Color Temp Virtual Dimmer - to be used with Color Temp via Virtual Dimmer SmartApp to make color temp adjustments in common SmartApps
 *
 *  Author: Scott Gibson
 *
 *  Date: 2015-03-12
 */
metadata {
	definition (name: "Color Temp Virtual Dimmer", namespace: "sticks18", author: "Scott Gibson") {
		capability "Actuator"
        capability "Switch Level"
        capability "Switch"
        
        command "updateTemp"
        
        attribute "kelvin", "number"
	}

	preferences {
    	input "minKelvin", "number", description: "Minimum", title: "Minimum Supported Color Temperature (Default 2700k)", range: "*..*", displayDuringSetup: false, required: true
    	input "maxKelvin", "number", description: "Maximum", title: "Maximum Supported Color Temperature (Default 6500k)", range: "*..*", displayDuringSetup: false, required: true
    }
	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles(scale: 2) {
    multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
    tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
        attributeState "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
        attributeState "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
        attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff"
        attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn"
    }
    tileAttribute ("device.level", key: "SLIDER_CONTROL") {
        attributeState "level", action:"switch level.setLevel"
    }
    tileAttribute ("kelvin", key: "SECONDARY_CONTROL") {
    	attributeState "device.kelvin", label:'${currentValue}k'
    }
}
		main "switch"
		details "switch"
	}
}

def parse(String description) {
}

def setLevel(value) {
    log.debug "Setting level to ${value}"
    sendEvent(name: "level", value: value)
    parent.setLevel(this, value, minKelvin, maxKelvin)
}

def on() {
	log.debug "turning on"
    sendEvent(name: "switch", value: "on")
    parent.turnOn(this)
}

def off() {
	log.debug "turning off"
    sendEvent(name: "switch", value: "off")
    parent.turnOff(this)
}

def updateTemp(value) {
	sendEvent(name: "kelvin", value: value)
}
