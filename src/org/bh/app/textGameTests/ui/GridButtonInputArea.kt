@file:Suppress("unused")

package org.bh.app.textGameTests.ui

import LatteFX.*
import javafx.event.*
import javafx.scene.control.*
import javafx.scene.layout.*
import org.bh.tools.base.abstraction.*
import org.bh.tools.base.collections.extensions.*
import org.bh.tools.base.func.*
import org.bh.tools.base.math.*
import org.bh.tools.base.math.geometry.*
import org.bh.tools.textGame.basics.*
import org.bh.tools.textGame.interaction.*
import org.bh.tools.textGame.interaction.InteractionFilter.*
import org.bh.tools.ui.events.*
import org.bh.tools.ui.visualization.*
import kotlin.DeprecationLevel.*

/**
 * A grid of buttons, to be used as a generalized user input
 *
 * @author Ben Leggiero
 * @since 2018-03-22
 */
class GridButtonInputArea
    (val model: GridButtonInputAreaModel)
    : GridPane(), TextGameUiElement<GridButtonInputUserAction> {

    val models: List<List<GridButtonInputModel>> by lazy {
        val actionTable = MutableList<List<GridButtonInputModel>>()

        (0.int8Value until model.rowCount).forEach { rowIndex32 ->
            val row = MutableList<GridButtonInputModel>()
            val rowIndex = rowIndex32.int8Value

            (0.int8Value until model.columnCount).forEach { columnIndex ->
                row.add(model.buttonModelGenerator(columnIndex.int8Value, rowIndex))
            }

            actionTable.add(row)
        }

        /*return*/ actionTable
    }


    val delegate: Delegate? = null


    init {
        models.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { columnIndex, cell ->
                super.add(cell.asButton(), columnIndex, rowIndex)
            }
        }
    }


    override fun interactionTriggers(filter: InteractionFilter): List<GridButtonInputTrigger> {
        return models
                .flatten()
                .filter { it.matches(filter) }
                .map { it.trigger }
    }


    override fun attemptInteraction(interaction: GridButtonInputUserAction): InteractionResult<GridButtonInputUserAction> {
        return delegate?.userDidPressButton(column = interaction.clickedColumn, row = interaction.clickedRow)
                ?: error("User interacted but programmer didn't encode any reaction")
    }


    override fun textDescription(kind: DescriptionPresentation): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


//    override fun
//            <InteractableType, InteractionType, Result>
//            userDidInteract(
//                interactable: InteractableType,
//                didPress: InteractionType)
//            : Result
//            where InteractableType : Interactable<InteractionType>,
//                  InteractionType : Interaction,
//                  Result : InteractionResult<InteractionType>
//    {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }



    interface Delegate {
        fun userDidPressButton(column: Int8, row: Int8): InteractionResult<GridButtonInputUserAction>
    }
}



interface ButtonInputUserAction: Interaction, UIEvent



interface GridButtonInputUserAction: ButtonInputUserAction {
    val clickedColumn: Int8
    val clickedRow: Int8
}



typealias GridButtonInputTrigger = InteractionTrigger.coordinates<Int8>



class GridButtonInputAreaModel (
        val columnCount: Int8,
        val rowCount: Int8,
        val buttonModelGenerator: (columnIndex: Int8, rowIndex: Int8) -> GridButtonInputModel
)



open class ButtonInputModel<Trigger, Action> (
        var titleText: String,
        var presentation: UIPresentation,
        private var interactionFilter: InteractionFilter,
        val trigger: Trigger,
        open val didPress: (Action) -> Unit
)
    where
        Trigger : InteractionTrigger,
        Action: ButtonInputUserAction
{
    open fun matches(filter: InteractionFilter) = when (this.interactionFilter) {
        all -> true
        currentlyAvailable -> filter == visibleToCharacter || filter == currentlyAvailable
        visibleToCharacter -> filter == visibleToCharacter
    }

    open fun asButton(eventTranslator: (ActionEvent) -> Action): Button {
        val b = Button(this.titleText)

        b.onAction = EventHandler {
            this.didPress(eventTranslator(it))
        }

        return b
    }
}


/**
 * Models a single button in a [GridButtonInputArea]
 */
open class GridButtonInputModel(
        val column: Int8,
        val row: Int8,
        titleText: String,
        presentation: UIPresentation = UIPresentation.default,
        interactionFilter: InteractionFilter,
        override val didPress: (GridButtonInputUserAction) -> Unit
): ButtonInputModel<GridButtonInputTrigger, GridButtonInputUserAction>
    (titleText, presentation, interactionFilter, GridButtonInputTrigger(Int8Point(column, row)), didPress = didPress)
{
    @Deprecated("This subclass comes with its own event translator", ReplaceWith("asButton()"), HIDDEN)
    override fun asButton(eventTranslator: (ActionEvent) -> GridButtonInputUserAction): Button {
        return super.asButton(eventTranslator)
    }


    fun asButton(): Button {
        return super.asButton { _ ->
            object : GridButtonInputUserAction {
                override val clickedColumn get() = column
                override val clickedRow get() = row
                override val trigger get() = GridButtonInputTrigger(Int8Point(x = column, y = row))
            }
        }
    }
}
