package com.codingspezis.android.metalonly.player.utils.jsonapi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <pre>
 * {
 *  "plan": [
 *   {
 *    "day": "29.07.13",
 *    "time": "00:00",
 *    "duration": 15,
 *    "moderator": "MetalHead",
 *    "show": "Keine Gruesse und Wuensche moeglich.",
 *    "genre": "Mixed Metal"
 *   }
 *  ]
 * }
 * </pre>
 */
@JsonAutoDetect
public class Plan {
    public Plan(){/*Explicit default constructor for Jackson */ }

    PlanEntry[] plan = {};

    public PlanEntry[] getPlan() {
        return plan;
    }

    @JsonProperty("plan")
    public void setPlan(PlanEntry[] plan) {
        this.plan = plan;
    }

    public static Plan getDefault() {
        return new Plan();
    }


}
