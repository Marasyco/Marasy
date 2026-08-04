package com.blueray.marasy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blueray.marasy.R
import com.blueray.marasy.databinding.AttributeItemBinding
import com.blueray.marasy.model.MainAttribute
import com.blueray.marasy.model.Option
import com.blueray.marasy.model.Variation

class AttributesAdapter(
    private val context: Context,
    private val attributes: MutableList<MainAttribute>,
    private val allVariations: List<Variation>,
    private val rootAttributeId: String? = null,          // if null, we'll use the first attribute in list as root
    private val placeholderLabel: String = "Select",      // or "اختر"
    private val onFiltered: (List<Variation>) -> Unit
) : RecyclerView.Adapter<AttributesAdapter.AttributeViewHolder>() {

    companion object {
        private const val PLACEHOLDER_ID = "__none__"
    }

    // attribute_id -> selected option id (only for real picks)
    private val selectedOptions = mutableMapOf<String, String>()

    // attributes that the user has committed (constrain others)
    private val committed = mutableSetOf<String>()

    // immutable copy of original options per attribute
    private val originalAttributes: List<MainAttribute> =
        attributes.map { it.copy(options = it.options.toList()) }

    // resolved root id
    private val rootId: String = rootAttributeId
        ?: attributes.firstOrNull()?.attribute_id
        ?: ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributeViewHolder {
        val binding = AttributeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttributeViewHolder(binding)
    }

    override fun getItemCount(): Int = attributes.size

    override fun onBindViewHolder(holder: AttributeViewHolder, position: Int) {
        holder.bind(attributes[position])
    }

    inner class AttributeViewHolder(private val binding: AttributeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var suppressSelectionCallback = false

        fun bind(attribute: MainAttribute) {
            binding.name.text = attribute.attribute_name

            val labels = attribute.options.map { it.attribute_value_label }
            val spinnerAdapter = ArrayAdapter(context, R.layout.spinner_item, labels)
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.optionsSpinner.adapter = spinnerAdapter

            // desired selection or placeholder
            val desiredId = selectedOptions[attribute.attribute_id]
            val indexToSelect = if (desiredId == null) 0
            else attribute.options.indexOfFirst { it.attribute_value_id == desiredId }.takeIf { it >= 0 } ?: 0

            suppressSelectionCallback = true
            binding.optionsSpinner.setSelection(indexToSelect)
            suppressSelectionCallback = false

            binding.optionsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                    if (suppressSelectionCallback) return

                    val option = attribute.options.getOrNull(pos) ?: return
                    val attrId = attribute.attribute_id
                    val valueId = option.attribute_value_id

                    if (valueId == PLACEHOLDER_ID) {
                        // user cleared this attribute
                        val changed = (selectedOptions.remove(attrId) != null) or committed.remove(attrId)
                        if (attrId == rootId) {
                            // clearing root removes all constraints & selections
                            selectedOptions.clear()
                            committed.clear()
                        }
                        if (changed || attrId == rootId) {
                            recomputeAllOptions(lockedAttrId = null)
                            onFiltered(filterVariations())
                        }
                        return
                    }

                    val prev = selectedOptions[attrId]
                    if (prev == valueId) return

                    // user selected a real value
                    if (attrId == rootId) {
                        // root changed: reset every other attr to placeholder (no constraints from them)
                        selectedOptions.clear()
                        committed.clear()
                        selectedOptions[attrId] = valueId
                        committed.add(attrId)
                        recomputeAllOptions(lockedAttrId = attrId)
                        
                        // After root selection, try to auto-select remaining single options
                        autoSelectRemainingSingleOptions()
                        
                        onFiltered(filterVariations())
                    } else {
                        selectedOptions[attrId] = valueId
                        committed.add(attrId)
                        recomputeAllOptions(lockedAttrId = attrId)
                        
                        // After recomputing, try to auto-select remaining single options
                        autoSelectRemainingSingleOptions()
                        
                        onFiltered(filterVariations())
                    }
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }
    }

    /** Build placeholder option */
    private fun placeholderOption(): Option = Option(
        attribute_value_id = PLACEHOLDER_ID,
        attribute_value_label = placeholderLabel
    )

    /** Ensure a placeholder is present at the start of an options list */
    private fun withPlaceholder(options: List<Option>): List<Option> {
        return if (options.firstOrNull()?.attribute_value_id == PLACEHOLDER_ID) options
        else listOf(placeholderOption()) + options
    }

    /** Selections that constrain others (committed only), optionally excluding one attr */
    private fun activeSelections(excludeAttrId: String? = null): Map<String, String> {
        return selectedOptions.filter { (k, _) -> committed.contains(k) && k != excludeAttrId }
    }

    /** Variations that satisfy committed selections only (including root if committed) */
    private fun filterVariations(): List<Variation> {
        val constraints = activeSelections()
        if (constraints.isEmpty()) return allVariations

        return allVariations.filter { variation ->
            constraints.all { (parentAttrId, selectedValueId) ->
                val parentName = originalAttributes.find { it.attribute_id == parentAttrId }?.attribute_name?.trim()
                    ?: return@all false
                variation.attributes.any { attr ->
                    (attr.attribute_id == selectedValueId) && (attr.attribute_name?.trim() == parentName)
                }
            }
        }
    }

    /**
     * Recompute domains for all attributes.
     * - Root attribute ALWAYS shows its full original domain (with placeholder), independent of others.
     * - Other attributes show domains compatible with committed selections (root included if committed).
     * - The locked attribute (user-just-changed) won't be auto-corrected in this pass.
     */
    private fun recomputeAllOptions(lockedAttrId: String? = null) {
        var changed = true
        var guard = 0

        while (changed && guard < 10) {
            changed = false
            guard++

            attributes.forEachIndexed { index, mainAttr ->
                val isRoot = mainAttr.attribute_id == rootId

                val displayOptions: List<Option> = if (isRoot) {
                    // Root is untied: always show full original domain
                    val full = originalAttributes.find { it.attribute_id == rootId }?.options ?: mainAttr.options
                    withPlaceholder(full)
                } else {
                    // Compute options compatible with committed "others"
                    val others = activeSelections(excludeAttrId = mainAttr.attribute_id)
                    val variationsMatchingOthers =
                        if (others.isEmpty()) allVariations
                        else allVariations.filter { variation ->
                            others.all { (parentAttrId, selectedValueId) ->
                                val parentName = originalAttributes.find { it.attribute_id == parentAttrId }?.attribute_name?.trim()
                                    ?: return@all false
                                variation.attributes.any { attr ->
                                    (attr.attribute_id == selectedValueId) && (attr.attribute_name?.trim() == parentName)
                                }
                            }
                        }

                    var possible = variationsMatchingOthers
                        .flatMap { variation ->
                            variation.attributes
                                .filter { it.attribute_name?.trim() == mainAttr.attribute_name.trim() }
                                .map { attr -> Option(attr.attribute_id, attr.attribute_label) }
                        }
                        .distinctBy { it.attribute_value_id }
                        .ifEmpty {
                            // Fallback to original domain
                            originalAttributes.find { it.attribute_id == mainAttr.attribute_id }?.options ?: emptyList()
                        }

                    // If locked (user just changed), ensure their current pick stays visible
                    if (mainAttr.attribute_id == lockedAttrId) {
                        val lockedId = selectedOptions[lockedAttrId]
                        if (lockedId != null && possible.none { it.attribute_value_id == lockedId }) {
                            originalAttributes.find { it.attribute_id == lockedAttrId }
                                ?.options
                                ?.find { it.attribute_value_id == lockedId }
                                ?.let { possible = listOf(it) + possible }
                        }
                    }

                    withPlaceholder(possible)
                }

                // Update row if options changed
                val currentIds = mainAttr.options.map { it.attribute_value_id }
                val newIds = displayOptions.map { it.attribute_value_id }
                if (currentIds != newIds) {
                    attributes[index] = mainAttr.copy(options = displayOptions)
                    notifyItemChanged(index)
                    changed = true
                }

                // Selection auto-correction rules
                val currentSelectedId = selectedOptions[mainAttr.attribute_id]

                if (mainAttr.attribute_id == lockedAttrId) {
                    // don't auto-correct the one user just changed
                    return@forEachIndexed
                }

                if (isRoot) {
                    // Root: never auto-correct; if committed but value vanished (shouldn't happen), keep selection map as-is.
                    // If root is not committed, ensure there's no selection stored.
                    if (!committed.contains(rootId) && currentSelectedId != null) {
                        selectedOptions.remove(rootId)
                        changed = true
                    }
                } else {
                    val isCommitted = committed.contains(mainAttr.attribute_id)
                    val realOptions = displayOptions.filter { it.attribute_value_id != PLACEHOLDER_ID }

                    if (isCommitted) {
                        val stillValid = currentSelectedId != null && realOptions.any { it.attribute_value_id == currentSelectedId }
                        if (!stillValid) {
                            if (realOptions.isNotEmpty()) {
                                selectedOptions[mainAttr.attribute_id] = realOptions.first().attribute_value_id
                                changed = true
                            } else {
                                // no valid options ⇒ uncommit & clear selection
                                committed.remove(mainAttr.attribute_id)
                                selectedOptions.remove(mainAttr.attribute_id)
                                changed = true
                            }
                        }
                    } else {
                        // not committed ⇒ ensure no stored selection (keep placeholder)
                        if (currentSelectedId != null) {
                            selectedOptions.remove(mainAttr.attribute_id)
                            changed = true
                        }
                    }
                }
            }
        }
    }

    fun initSelections() {
        // Start with NO selections. Populate each spinner with placeholder + full domain.
        attributes.forEachIndexed { index, attr ->
            val initial = withPlaceholder(
                originalAttributes.find { it.attribute_id == attr.attribute_id }?.options ?: attr.options
            )
            attributes[index] = attr.copy(options = initial)
            notifyItemChanged(index)
        }

        // Initial: root untied + no committed constraints
        recomputeAllOptions(lockedAttrId = null)
        
        // Auto-select if there's only one variation or if all attributes have single options
        autoSelectIfPossible()
        
        // Notify all items to update UI after auto-selection
        if (selectedOptions.isNotEmpty()) {
            attributes.indices.forEach { notifyItemChanged(it) }
        }
        
        onFiltered(filterVariations())
    }
    
    /**
     * Auto-selects attributes when:
     * 1. There's only one variation total
     * 2. All attributes have only one option (excluding placeholder)
     * 3. After a selection, remaining attributes have only one valid option
     */
    private fun autoSelectIfPossible() {
        // Case 1: Only one variation - auto-select all attributes
        if (allVariations.size == 1) {
            val singleVariation = allVariations.first()
            originalAttributes.forEach { attr ->
                val matchingAttr = singleVariation.attributes.find { 
                    it.attribute_name?.trim() == attr.attribute_name.trim() 
                }
                matchingAttr?.let { match ->
                    // Find the option that matches the variation's attribute value id
                    val option = attr.options.find { opt -> opt.attribute_value_id == match.attribute_id }
                    option?.let {
                        selectedOptions[attr.attribute_id] = it.attribute_value_id
                        committed.add(attr.attribute_id)
                    }
                }
            }
            recomputeAllOptions(lockedAttrId = null)
            return
        }
        
        // Case 2: Auto-select attributes with only one option (excluding placeholder)
        attributes.forEach { attr ->
            val realOptions = attr.options.filter { it.attribute_value_id != PLACEHOLDER_ID }
            if (realOptions.size == 1 && !committed.contains(attr.attribute_id)) {
                val singleOption = realOptions.first()
                selectedOptions[attr.attribute_id] = singleOption.attribute_value_id
                committed.add(attr.attribute_id)
            }
        }
        
        // Case 3: After selections, auto-select remaining attributes with single valid option
        autoSelectRemainingSingleOptions()
    }
    
    /**
     * After user selections, auto-select any remaining attributes that have only one valid option
     */
    private fun autoSelectRemainingSingleOptions() {
        var changed = true
        var iterations = 0
        while (changed && iterations < 5) {
            changed = false
            iterations++
            
            attributes.forEachIndexed { index, attr ->
                if (committed.contains(attr.attribute_id)) return@forEachIndexed
                
                val realOptions = attr.options.filter { it.attribute_value_id != PLACEHOLDER_ID }
                if (realOptions.size == 1) {
                    val singleOption = realOptions.first()
                    if (selectedOptions[attr.attribute_id] != singleOption.attribute_value_id) {
                        selectedOptions[attr.attribute_id] = singleOption.attribute_value_id
                        committed.add(attr.attribute_id)
                        changed = true
                        // Notify UI to update this spinner
                        notifyItemChanged(index)
                    }
                }
            }
            
            if (changed) {
                recomputeAllOptions(lockedAttrId = null)
            }
        }
    }

    // Unique matching variation for current selections; null if ambiguous or none.
    fun getSelectedVariation(): Variation? {
        if (selectedOptions.isEmpty()) return null

        val idToName = originalAttributes.associate { it.attribute_id to it.attribute_name.trim() }
        val candidates = allVariations.filter { variation ->
            selectedOptions.all { (parentAttrId, selectedValueId) ->
                val parentName = idToName[parentAttrId] ?: return@all false
                variation.attributes.any { attr ->
                    (attr.attribute_name?.trim() == parentName) && (attr.attribute_id == selectedValueId)
                }
            }
        }
        return candidates.firstOrNull()
    }

    fun getSelectedVid(): String? = getSelectedVariation()?.vid
}
