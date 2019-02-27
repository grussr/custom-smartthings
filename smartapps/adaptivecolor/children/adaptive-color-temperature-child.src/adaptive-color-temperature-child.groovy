definition(
    name: "Adaptive Color Temperature Child",
    namespace: "adaptivecolor/children",
    author: "Ryan Gruss",
    description: "Do not install!  Sets color temperature of selected bulbs based on brightness",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	page name: "mainPage", title: "Select color temp lights and temperature ranges", install: false, uninstall: true, nextPage: "namePage"
    page name: "namePage", title: "Name", install: true, uninstall: true
}

def mainPage() {
	dynamicPage(name: "mainPage"){
		section("Adaptive Color Temperature") {
            paragraph "Add color temperature lights here.  As brightness increases/decreases, color temperature will be adjusted accordingly."
			input "cLights", "capability.color temperature", title: "Select Color Temp Light", required: true, multiple: true
  			input "minKelvin", "number", description: "Minimum", title: "Minimum Supported Color Temperature (Default 2700k)", range: "*..*", displayDuringSetup: false, defaultValue: 2700
    		input "maxKelvin", "number", description: "Maximum", title: "Maximum Supported Color Temperature (Default 6500k)", range: "*..*", displayDuringSetup: false, defaultValue: 6500
			input "colorChangeDelay", "number", description: "Delay in ms", title: "Delay in ms before changing temperatures (needed by some bulbs like Ikea TRADFRI)", range: "0..*", displayDuringSetup: false, defaultValue: 0
        }
	}
}

def namePage() {
    if (!overrideLabel) {
        // if the user selects to not change the label, give a default label
        def l = defaultLabel()
        log.debug "will set default label of $l"
        app.updateLabel(l)
    }
    dynamicPage(name: "namePage") {
        if (overrideLabel) {
            section("Automation name") {
                label title: "Enter custom name", defaultValue: app.label, required: false
            }
        } else {
            section("Automation name") {
                paragraph app.label
            }
        }
        section {
            input "overrideLabel", "bool", title: "Edit automation name", defaultValue: "false", required: "false", submitOnChange: true
        }
    }
}

def defaultLabel() {
    def tempLabel = cLights.size() == 1 ? cLights[0].displayName : cLights[0].displayName + ", etc..."
    "Adjust ${tempLabel} color temperature from ${minKelvin}K to ${maxKelvin}K"
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(cLights, "switch.setLevel", onSetLevel)
    subscribe(cLights, "level", onSetLevel)
}

def uninstalled() {
}

def onSetLevel(evt) {
    setLevel(evt.device, evt.value.toFloat())
}

def setLevel(childDevice, value) {
    def degrees = Math.round((value * ((maxKelvin - minKelvin) / 100)) + minKelvin)
  	log.debug "Converting dimmer level ${value} to color temp ${degrees} after a ${colorChangeDelay}ms delay..."
    pause(colorChangeDelay)
    childDevice.setColorTemperature(degrees)
}