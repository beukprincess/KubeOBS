package com.example.kubeobs.data

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
sealed interface PodsRefState{
    object IdlePodsRef: PodsRefState
    object LoadingPodsRef: PodsRefState
    data class SuccessPodsRef(val data: PodsResponse?): PodsRefState
    data class ErrorPodsRef(val e: String): PodsRefState
}
sealed interface PodHealthUIState{
    object LoadingPodHealth: PodHealthUIState
    data class SuccessPodHealth(val data: PodsInfoResponse?): PodHealthUIState
    data class ErrorPodHealth(val e: String): PodHealthUIState
}
sealed interface MetricsUIState{
    object LoadingMetrics: MetricsUIState
    data class SuccessMetrics(val data: MetricsResponse?): MetricsUIState
    data class ErrorMetrics(val e: String): MetricsUIState
}
sealed interface MetricsDataState{
    object LoadingMetricsData: MetricsDataState
    data class SuccessMetricsData(val data: MetricsResponse?): MetricsDataState
    data class ErrorMetricsData(val e: String): MetricsDataState
}
sealed interface RegResultState {
    object IdleResult : RegResultState
    object LoadingResult : RegResultState
    data class SuccessResult(val data: RegisterResponse?) : RegResultState
    data class ErrorResult(val e: String) : RegResultState
}
sealed interface LoginResultState {
    object IdleResult : LoginResultState
    object LoadingResult : LoginResultState
    data class SuccessResult(val token: String) : LoginResultState
    data class ErrorResult(val e: String) : LoginResultState
}
sealed class ClustersState {
    object IdleResult : ClustersState()
    object LoadingResult : ClustersState()
    data class SuccessResult(val clusters: List<ClusterResponse>) : ClustersState()
    data class ErrorResult(val e: String) : ClustersState()
}

sealed class AddClusterState {
    object IdleResult : AddClusterState()
    object LoadingResult : AddClusterState()
    data class SuccessResult(val cluster: ClusterResponse) : AddClusterState()
    data class ErrorResult(val e: String) : AddClusterState()
}