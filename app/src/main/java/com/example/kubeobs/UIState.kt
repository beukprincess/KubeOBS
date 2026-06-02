package com.example.kubeobs

sealed interface NodesUIState{
    object LoadingNodes: NodesUIState
    data class SuccessNodes(val data: NodesResponse?): NodesUIState
    data class ErrorNodes(val e: String): NodesUIState
}
sealed interface PodsUIState{
    object LoadingPods: PodsUIState
    data class SuccessPods(val data: PodsResponse?): PodsUIState
    data class ErrorPods(val e: String): PodsUIState
}
sealed interface PodHealthUIState{
    object LoadingPodHealth: PodHealthUIState
    data class SuccessPodHealth(val data: PodsInfoResponse?): PodHealthUIState
    data class ErrorPodHealth(val e: String): PodHealthUIState
}