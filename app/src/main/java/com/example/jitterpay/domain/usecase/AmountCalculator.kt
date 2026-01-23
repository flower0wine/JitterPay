package com.example.jitterpay.domain.usecase

import com.example.jitterpay.domain.model.Money
import java.math.BigDecimal
import javax.inject.Inject

/**
 * 金额计算器 - 处理输入序列和表达式计算
 *
 * 职责：
 * - 管理用户输入的数字序列
 * - 计算加减表达式
 * - 提供格式化后的显示值
 *
 * 设计原则：
 * - 纯业务逻辑，无 UI 依赖
 * - 全程使用 Money 对象，确保精度
 * - 可独立进行单元测试
 */
class AmountCalculator @Inject constructor() {

    /**
     * 输入类型
     */
    sealed interface Input {
        data class Digit(val value: String) : Input  // 0-9
        data class Operator(val value: String) : Input  // +, -
        data object Decimal : Input
        data object Backspace : Input
        data object Clear : Input
    }

    /**
     * 计算器状态
     */
    data class State(
        val displayValue: String = "",      // 当前显示的原始输入，如 "12+34.5"
        val currentAmount: Money = Money.ZERO,  // 计算后的当前金额
        val isEmpty: Boolean = true
    ) {
        companion object {
            val INITIAL = State()
        }
    }

    private var state: State = State.INITIAL

    /**
     * 处理输入
     */
    fun process(input: Input): State {
        state = when (input) {
            is Input.Digit -> handleDigit(input.value)
            is Input.Operator -> handleOperator(input.value)
            is Input.Decimal -> handleDecimal()
            is Input.Backspace -> handleBackspace()
            is Input.Clear -> State.INITIAL
        }
        return state
    }

    /**
     * 获取当前金额
     */
    fun getCurrentAmount(): Money = state.currentAmount

    /**
     * 获取当前显示值
     */
    fun getDisplayValue(): String = state.displayValue

    /**
     * 重置计算器
     */
    fun reset() {
        state = State.INITIAL
    }

    private fun handleDigit(digit: String): State {
        val current = state.displayValue

        return when {
            current.isEmpty() -> {
                // 初始状态，直接设置数字
                State(
                    displayValue = digit,
                    currentAmount = Money.fromBigDecimal(BigDecimal(digit)),
                    isEmpty = false
                )
            }
            // 以运算符结尾：新数字开始新部分
            current.last() in "+-" -> {
                val newDisplay = current + digit
                val newAmount = calculateExpression(newDisplay)
                state.copy(
                    displayValue = newDisplay,
                    currentAmount = newAmount,
                    isEmpty = false
                )
            }
            // 正常拼接数字
            else -> {
                // 限制最大长度（防止溢出）
                if (current.length >= MAX_INPUT_LENGTH) {
                    return state
                }
                val newDisplay = current + digit
                val newAmount = calculateExpression(newDisplay)
                state.copy(
                    displayValue = newDisplay,
                    currentAmount = newAmount,
                    isEmpty = false
                )
            }
        }
    }

    private fun handleOperator(op: String): State {
        val current = state.displayValue

        return when {
            current.isEmpty() -> {
                // 初始状态输入运算符，设置为 0 + op
                State(
                    displayValue = "0$op",
                    currentAmount = Money.ZERO,
                    isEmpty = false
                )
            }
            current.last() in "+-" -> {
                // 连续输入运算符，替换最后一个
                State(
                    displayValue = current.dropLast(1) + op,
                    currentAmount = state.currentAmount,
                    isEmpty = false
                )
            }
            else -> {
                // 正常添加运算符
                State(
                    displayValue = current + op,
                    currentAmount = state.currentAmount,
                    isEmpty = false
                )
            }
        }
    }

    private fun handleDecimal(): State {
        val current = state.displayValue

        return when {
            current.isEmpty() -> {
                // 初始状态输入小数点
                State(
                    displayValue = "0.",
                    currentAmount = Money.ZERO,
                    isEmpty = false
                )
            }
            current.contains(".") -> {
                // 已有小数点，检查是否在当前数字部分
                val parts = current.split(Regex("(?=[+-])"))
                val lastPart = parts.lastOrNull() ?: ""
                if (lastPart.contains(".")) {
                    state // 已有小数点，忽略
                } else {
                    State(
                        displayValue = "$current.",
                        currentAmount = state.currentAmount,
                        isEmpty = false
                    )
                }
            }
            current.last() in "+-" -> {
                // 运算符后输入小数点
                State(
                    displayValue = current + "0.",
                    currentAmount = state.currentAmount,
                    isEmpty = false
                )
            }
            else -> {
                // 正常添加小数点
                State(
                    displayValue = "$current.",
                    currentAmount = state.currentAmount,
                    isEmpty = false
                )
            }
        }
    }

    private fun handleBackspace(): State {
        val current = state.displayValue

        return when {
            current.isEmpty() -> state
            current.length == 1 -> State.INITIAL
            else -> {
                val newDisplay = current.dropLast(1)
                val newAmount = calculateExpression(newDisplay)
                State(
                    displayValue = newDisplay,
                    currentAmount = newAmount,
                    isEmpty = newDisplay.isEmpty()
                )
            }
        }
    }

    /**
     * 计算表达式（支持 + 和 -）
     * 使用 Money 进行精确计算
     */
    private fun calculateExpression(expression: String): Money {
        if (expression.isEmpty()) return Money.ZERO

        val parts = expression.split(Regex("(?=[+-])"))
        var resultCents = parts[0]
            .toBigDecimalOrNull()
            ?.multiply(BigDecimal(100))
            ?.toLong() ?: 0L

        for (i in 1 until parts.size) {
            val part = parts[i]
            if (part.isEmpty()) continue
            val op = part[0]
            val valueCents = part.drop(1)
                .toBigDecimalOrNull()
                ?.multiply(BigDecimal(100))
                ?.toLong() ?: continue

            resultCents = when (op) {
                '+' -> resultCents + valueCents
                '-' -> resultCents - valueCents
                else -> resultCents
            }
        }

        return Money.fromCents(resultCents)
    }

    companion object {
        /**
         * 最大输入长度（防止整数部分溢出）
         */
        private const val MAX_INPUT_LENGTH = 20
    }
}
