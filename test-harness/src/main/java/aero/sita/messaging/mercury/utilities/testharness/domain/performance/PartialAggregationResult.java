/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.utilities.testharness.domain.performance;

// Maps only the avg/min/max part of the aggregation
public record PartialAggregationResult(Double averageMessageAcceptanceLatency,
                                       long maximumMessageAcceptanceLatency,
                                       long minimumMessageAcceptanceLatency,
                                       Double averageEndToEndLatency,
                                       long maximumEndToEndLatency,
                                       long minimumEndToEndLatency) {
}
