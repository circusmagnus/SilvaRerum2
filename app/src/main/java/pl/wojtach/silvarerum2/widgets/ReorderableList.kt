package pl.wojtach.silvarerum2.widgets

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import pl.wojtach.silvarerum2.utils.HasStableId
import kotlin.math.roundToInt

@Composable
fun <T : HasStableId> ReorderableList(
    reorderableItems: List<T>,
    ListCell: @Composable (Modifier, T) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit
) {
//    val lazyListState = rememberLazyListState()
//    var draggedElement by remember(key1 = reorderableItems) {
//        mutableStateOf<LazyListItemInfo?>(null)
//    }
//    var draggedElementIndex by remember(key1 = reorderableItems) {
//        mutableStateOf<Int>(-1)
//    }
//    var draggedDistanceY by remember(key1 = reorderableItems) {
//        mutableStateOf(0f)
//    }

    val state = rememberReorderableListState(onReorder = onReorder)

    LazyColumn(
        modifier = Modifier.pointerInput(key1 = Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    state.onDragStart(offset)
//                    lazyListState.layoutInfo.visibleItemsInfo
//                        .first { listItem -> offset.y.roundToInt() in listItem.offset..listItem.offset + listItem.size }
//                        .also { found -> draggedElement = found; draggedElementIndex = found.index }
                },
                onDrag = { _, dragAmount ->
                    state.onDrag(dragAmount)
//                    draggedDistanceY += dragAmount.y
                         },
                onDragEnd = {
                    state.onDragInterrupt()
//                    val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
//                    Log.d("lw", "onDragEnd, dragged dist: $draggedDistanceY, dragged element: $draggedElement")
//                    val toIndex = visibleItems.findDraggedToIndexOrNull(draggedElement!!, draggedDistanceY)
//                    toIndex?.let { onReorder(draggedElementIndex, it) }
//
//                    draggedDistanceY = 0f
                }
            )
        },
        state = state.lazyListState
    ) {
        itemsIndexed(
            reorderableItems,
            { _, item -> item.id },
            { index, item ->
                ListCell(
                    Modifier
                        .graphicsLayer { translationY = if (index == state.currentDraggedElementIndex) state.calculatedDisplacement else 0f }
                        .zIndex(if (index == state.currentDraggedElementIndex) 1f else 0f),
                    item
                )
            }
        )
    }
}

private fun List<LazyListItemInfo>.findDraggedToIndexOrNull(draggedItem: LazyListItemInfo, draggedDistanceY: Float): Int? {
    val isDraggingUp = draggedDistanceY < 0

    return if (isDraggingUp) firstOrNull { it.isHoveredByBottomOf(draggedItem, draggedDistanceY) }?.index
    else firstOrNull { it.isHoveredByTopOf(draggedItem, draggedDistanceY) }?.index
}

private fun LazyListItemInfo.isHoveredByBottomOf(draggedItem: LazyListItemInfo, afterDraggedBy: Float) =
    draggedItem.offset + draggedItem.size + afterDraggedBy.roundToInt() in offset..offset + size

private fun LazyListItemInfo.isHoveredByTopOf(draggedItem: LazyListItemInfo, afterDraggedBy: Float) =
    draggedItem.offset + afterDraggedBy.roundToInt() in offset..offset + size

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this.layoutInfo.visibleItemsInfo.getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)
}

@Composable
private fun rememberReorderableListState(
    lazyListState: LazyListState = rememberLazyListState(),
    onReorder: (Int, Int) -> Unit,
) = remember { ReorderableListState(lazyListState, onReorder) }

private class ReorderableListState(
    val lazyListState: LazyListState,
    private val onReorder: (fromIndex: Int, toIndex: Int) -> Unit
) {
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)
    var currentDraggedElementIndex by mutableStateOf<Int>(-1)
    private var draggedDistanceY by mutableStateOf(0f)
    private val currentlyDraggedElement: LazyListItemInfo?
        get() = lazyListState.getVisibleItemInfoFor(currentDraggedElementIndex)

    val calculatedDisplacement: Float
        get() = currentlyDraggedElement?.let { current ->
            (initiallyDraggedElement?.offset ?: 0) + draggedDistanceY - (current.offset)
        } ?: draggedDistanceY

    fun onDragStart(offset: Offset) {
        lazyListState.layoutInfo.visibleItemsInfo
            .first { listItem -> offset.y.roundToInt() in listItem.offset..listItem.offset + listItem.size }
            .also { found -> initiallyDraggedElement = found; currentDraggedElementIndex = found.index }
    }

    fun onDrag(dragAmount: Offset) {
        draggedDistanceY += dragAmount.y

        val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
        val toIndex2 = visibleItems.findDraggedToIndexOrNull(currentlyDraggedElement!!, calculatedDisplacement)

//        Log.d("lw", "onDragEnd, dragged dist: $draggedDistanceY, dragged element: $draggedElement")
//        val toIndex = visibleItems.findDraggedToIndexOrNull(draggedElement!!, draggedDistanceY)

        toIndex2
            ?.takeIf { newIndex -> newIndex != currentDraggedElementIndex }
            ?.let { newIndex ->
            onReorder(currentDraggedElementIndex, newIndex)
            currentDraggedElementIndex = newIndex
        }
    }

    fun onDragInterrupt(){
        draggedDistanceY = 0f
        initiallyDraggedElement = null
        currentDraggedElementIndex = -1
    }
}