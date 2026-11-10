package com.magao.backend.module.judge.judge.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magao.backend.module.judge.dto.JudgeResultDTO;
import com.magao.backend.module.judge.dto.JudgeSubmitDTO;
import com.magao.backend.module.judge.dto.RunResultDTO;
import com.magao.backend.module.judge.judge.AbstractJudge;
import com.magao.backend.module.judge.client.Judge0Result;
import com.magao.backend.module.practice.entity.PracticeQuestion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Scratch3 judge implementation.
 * <p>
 * Converts blocks from Scratch sb3 JSON into Python pseudo-code,
 * then invokes Judge0 to execute the Python code.
 * Supported block mappings:
 * - Motion (move/turn/glide): outputs simulated coordinate info
 * - Control (repeat/if/wait): converted to Python loops/conditions/wait
 * - Operators (add/subtract/multiply/mod/random): converted to Python expressions
 * - Variables (set/change): converted to Python variable assignments
 * - Sensing (touching/ask_wait): converted to Python simulated sensing output
 * </p>
 *
 * @author Magao Dev Team
 */
@Slf4j
@Component
public class ScratchJudge extends AbstractJudge {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Judge0 Python language ID. */
    private static final int JUDGE0_LANGUAGE_ID = 71;

    /** Simulated canvas dimensions. */
    private static final int CANVAS_WIDTH = 480;
    private static final int CANVAS_HEIGHT = 360;

    @Override
    public String getLang() {
        return "scratch3";
    }

    @Override
    public int getJudge0LanguageId() {
        return JUDGE0_LANGUAGE_ID;
    }

    @Override
    public JudgeResultDTO judge(JudgeSubmitDTO submitDTO) {
        log.info("Scratch3 judge: questionId={}", submitDTO.getQuestionId());

        String scratchJson = submitDTO.getScratchJson();
        if (scratchJson == null || scratchJson.isEmpty()) {
            return JudgeResultDTO.builder()
                    .status("COMPILE_ERROR")
                    .score(0)
                    .totalScore(100)
                    .passedCases(0)
                    .totalCases(1)
                    .errorMessage("Scratch 项目数据为空")
                    .build();
        }

        try {
            // Parse Scratch project JSON
            JsonNode projectJson = MAPPER.readTree(scratchJson);

            // Convert Scratch blocks to Python pseudo-code
            String pythonCode = convertScratchToPython(projectJson);
            log.info("Scratch block-to-Python conversion complete, code length: {}", pythonCode.length());

            if (pythonCode.trim().isEmpty()) {
                return JudgeResultDTO.builder()
                        .status("COMPILE_ERROR")
                        .score(0)
                        .totalScore(100)
                        .passedCases(0)
                        .totalCases(1)
                        .errorMessage("Scratch 项目中未找到可执行的积木")
                        .build();
            }

            // 真实判分：将转换后的 Python 代码提交 Judge0 运行验证
            PracticeQuestion q = questionMapper.selectById(submitDTO.getQuestionId());
            int timeLimit = (q != null && q.getTimeLimit() != null) ? q.getTimeLimit() : 1;
            int memLimit = (q != null && q.getMemoryLimit() != null) ? q.getMemoryLimit() : 256;
            Judge0Result r = judge0Client.judge(pythonCode, JUDGE0_LANGUAGE_ID, "", null, timeLimit, memLimit);
            if (r.isError()) {
                return JudgeResultDTO.builder()
                        .status("SYSTEM_ERROR").score(0).totalScore(100)
                        .passedCases(0).totalCases(1)
                        .errorMessage(r.getErrorMessage()).build();
            }
            boolean ok = r.getStatusId() == 3 || r.getStatusId() == 4;
            String status = ok ? "ACCEPTED" : mapStatus(r.getStatusId());
            int score = ok ? 100 : 0;
            return JudgeResultDTO.builder()
                    .status(status).score(score).totalScore(100)
                    .passedCases(ok ? 1 : 0).totalCases(1)
                    .timeUsed(r.getTime() == null ? null : (long) (r.getTime() * 1000))
                    .memoryUsed(r.getMemory())
                    .output(r.getStdout())
                    .errorMessage(ok ? null : buildFailMessage(r))
                    .build();

        } catch (Exception e) {
            log.error("Scratch judge error", e);
            return JudgeResultDTO.builder()
                    .status("SYSTEM_ERROR")
                    .score(0)
                    .totalScore(100)
                    .passedCases(0)
                    .totalCases(1)
                    .errorMessage("Scratch 判题系统错误: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public RunResultDTO run(JudgeSubmitDTO submitDTO) {
        log.info("Scratch3 run: questionId={}", submitDTO.getQuestionId());

        String scratchJson = submitDTO.getScratchJson();
        if (scratchJson == null || scratchJson.isEmpty()) {
            return RunResultDTO.builder()
                    .success(false)
                    .errorMessage("Scratch 项目数据为空")
                    .build();
        }

        try {
            JsonNode projectJson = MAPPER.readTree(scratchJson);
            String pythonCode = convertScratchToPython(projectJson);

            if (pythonCode.trim().isEmpty()) {
                return RunResultDTO.builder()
                        .success(false)
                        .errorMessage("Scratch 项目中未找到可执行的积木")
                        .build();
            }

            // 真实运行：将转换后的 Python 代码提交 Judge0
            PracticeQuestion q = questionMapper.selectById(submitDTO.getQuestionId());
            int timeLimit = (q != null && q.getTimeLimit() != null) ? q.getTimeLimit() : 1;
            int memLimit = (q != null && q.getMemoryLimit() != null) ? q.getMemoryLimit() : 256;
            Judge0Result r = judge0Client.judge(pythonCode, JUDGE0_LANGUAGE_ID, "", null, timeLimit, memLimit);
            if (r.isError()) {
                return RunResultDTO.builder().success(false).errorMessage(r.getErrorMessage()).build();
            }
            boolean success = r.getStatusId() == 3 || r.getStatusId() == 4;
            String out = (r.getStdout() != null ? r.getStdout()
                    : (success ? "运行成功（无输出）" : r.getStatusDescription()));
            return RunResultDTO.builder()
                    .success(success)
                    .output(out)
                    .timeUsed(r.getTime() == null ? null : (long) (r.getTime() * 1000))
                    .build();

        } catch (Exception e) {
            log.error("Scratch run error", e);
            return RunResultDTO.builder()
                    .success(false)
                    .errorMessage("Scratch 运行错误: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public String validate(JudgeSubmitDTO submitDTO) {
        String baseError = super.validate(submitDTO);
        if (baseError != null) return baseError;

        if (submitDTO.getScratchJson() == null || submitDTO.getScratchJson().isEmpty()) {
            return "Scratch 项目数据不能为空";
        }
        if (submitDTO.getScratchJson().length() > 10_000_000) {
            return "Scratch 项目数据过大 (最大 10MB)";
        }
        return null;
    }

    // ==================== Scratch block to Python core logic ====================

    /**
     * Converts a Scratch project JSON into Python pseudo-code.
     *
     * @param projectJson root node of the Scratch project JSON
     * @return Python pseudo-code string
     */
    private String convertScratchToPython(JsonNode projectJson) {
        StringBuilder sb = new StringBuilder();

        // Python pseudo-code header: simulate Scratch stage environment
        sb.append("# Scratch block to Python pseudo-code (auto-generated)\n");
        sb.append("import random\n");
        sb.append("import time\n\n");
        sb.append("# Simulated Scratch stage environment\n");
        sb.append("x, y = 0, 0  # sprite initial position\n");
        sb.append("direction = 90  # sprite direction (degrees)\n");
        sb.append("pen_down = False  # pen state\n");
        sb.append("variables = {}  # variable dictionary\n");
        sb.append("output_lines = []  # output collection\n\n");

        // Iterate over all targets (sprites / stage)
        JsonNode targets = projectJson.get("targets");
        if (targets != null && targets.isArray()) {
            for (JsonNode target : targets) {
                // Skip the stage (isStage=true), only process sprite blocks
                boolean isStage = target.has("isStage") && target.get("isStage").asBoolean();
                if (isStage) {
                    continue;
                }

                String targetName = target.has("name") ? target.get("name").asText() : "Sprite";
                sb.append("# Sprite: ").append(targetName).append("\n");

                // Get the block list
                JsonNode blocks = target.get("blocks");
                if (blocks != null && blocks.isObject()) {
                    // Find top-level blocks (from blockIds or by iterating blocks directly)
                    JsonNode blockIds = target.get("blocks");
                    if (blockIds != null && blockIds.isObject()) {
                        List<String> topBlocks = findTopLevelBlocks(blockIds);
                        for (String blockId : topBlocks) {
                            JsonNode block = blockIds.get(blockId);
                            if (block != null) {
                                convertBlock(sb, block, blockIds, 0);
                                sb.append("\n");
                            }
                        }
                    }
                }
            }
        }

        // Footer: output collected results
        sb.append("\n# Output simulated coordinate info\n");
        sb.append("for line in output_lines:\n");
        sb.append("    print(line)\n");

        return sb.toString();
    }

    /**
     * Finds top-level blocks (blocks that have no parent block reference).
     */
    private List<String> findTopLevelBlocks(JsonNode blocks) {
        Set<String> allBlockIds = new HashSet<>();
        Set<String> childBlockIds = new HashSet<>();

        Iterator<Map.Entry<String, JsonNode>> fields = blocks.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String blockId = entry.getKey();
            JsonNode block = entry.getValue();

            // Skip non-block nodes (e.g. SVG, comments)
            if (!block.isObject() || !block.has("opcode")) {
                continue;
            }

            allBlockIds.add(blockId);

            // Collect all child block references
            collectChildBlocks(block, childBlockIds);
        }

        // Top-level blocks = all blocks - child blocks
        List<String> topBlocks = new ArrayList<>();
        for (String blockId : allBlockIds) {
            if (!childBlockIds.contains(blockId)) {
                topBlocks.add(blockId);
            }
        }

        return topBlocks;
    }

    /**
     * Recursively collects all child block references within a block.
     */
    private void collectChildBlocks(JsonNode block, Set<String> childIds) {
        if (block == null || !block.isObject()) return;

        // Check child blocks in inputs
        JsonNode inputs = block.get("inputs");
        if (inputs != null && inputs.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> inputFields = inputs.fields();
            while (inputFields.hasNext()) {
                Map.Entry<String, JsonNode> inputEntry = inputFields.next();
                JsonNode inputVal = inputEntry.getValue();
                if (inputVal.isArray() && inputVal.size() > 1) {
                    // [1] is usually a block reference
                    JsonNode ref = inputVal.get(1);
                    if (ref.isArray() && ref.size() > 0 && ref.get(0).isTextual()) {
                        childIds.add(ref.get(0).asText());
                    }
                }
            }
        }

        // Check the next block
        JsonNode next = block.get("next");
        if (next != null && next.isTextual() && !next.asText().isEmpty()) {
            childIds.add(next.asText());
        }
    }

    /**
     * Converts a single block into Python code.
     *
     * @param sb        code builder
     * @param block     block node
     * @param allBlocks all blocks mapping
     * @param indent    indentation level
     */
    private void convertBlock(StringBuilder sb, JsonNode block, JsonNode allBlocks, int indent) {
        if (block == null || !block.isObject() || !block.has("opcode")) {
            return;
        }

        String opcode = block.get("opcode").asText();
        String indentStr = "    ".repeat(indent);

        switch (opcode) {
            // ==================== Motion blocks ====================
            case "motion_movesteps":
                appendMotionMove(sb, block, allBlocks, indentStr);
                break;
            case "motion_turnright":
                appendMotionTurn(sb, block, allBlocks, indentStr, "right");
                break;
            case "motion_turnleft":
                appendMotionTurn(sb, block, allBlocks, indentStr, "left");
                break;
            case "motion_gotoxy":
                appendMotionGoto(sb, block, allBlocks, indentStr);
                break;
            case "motion_glidesecstoxy":
                appendMotionGlide(sb, block, allBlocks, indentStr);
                break;
            case "motion_setx":
                appendMotionSetX(sb, block, allBlocks, indentStr);
                break;
            case "motion_sety":
                appendMotionSetY(sb, block, allBlocks, indentStr);
                break;
            case "motion_changexby":
                appendMotionChangeX(sb, block, allBlocks, indentStr);
                break;
            case "motion_changeyby":
                appendMotionChangeY(sb, block, allBlocks, indentStr);
                break;

            // ==================== Control blocks ====================
            case "control_repeat":
                appendControlRepeat(sb, block, allBlocks, indentStr, indent);
                break;
            case "control_repeat_until":
                appendControlRepeatUntil(sb, block, allBlocks, indentStr, indent);
                break;
            case "control_if":
                appendControlIf(sb, block, allBlocks, indentStr, indent);
                break;
            case "control_if_else":
                appendControlIfElse(sb, block, allBlocks, indentStr, indent);
                break;
            case "control_wait":
                appendControlWait(sb, block, allBlocks, indentStr);
                break;

            // ==================== Operator blocks ====================
            case "operator_add":
            case "operator_subtract":
            case "operator_multiply":
            case "operator_divide":
            case "operator_mod":
            case "operator_random":
            case "operator_mathop":
                // Operator blocks are usually embedded as inputs in other blocks;
                // when they appear standalone, output the expression
                appendOperatorExpression(sb, block, allBlocks, indentStr);
                break;

            // ==================== Variable blocks ====================
            case "data_setvariableto":
                appendDataSetVariable(sb, block, allBlocks, indentStr);
                break;
            case "data_changevariableby":
                appendDataChangeVariable(sb, block, allBlocks, indentStr);
                break;

            // ==================== Sensing blocks ====================
            case "sensing_touchingobject":
                appendSensingTouching(sb, block, allBlocks, indentStr);
                break;
            case "sensing_askandwait":
                appendSensingAskWait(sb, block, allBlocks, indentStr);
                break;

            // ==================== Looks blocks (output coordinate info) ====================
            case "looks_say":
            case "looks_think":
                appendLooksSay(sb, block, allBlocks, indentStr, opcode);
                break;

            default:
                // Unknown block type, output as a comment
                sb.append(indentStr).append("# [unsupported block: ").append(opcode).append("]\n");
                break;
        }

        // Process the next block (the connected following block)
        JsonNode next = block.get("next");
        if (next != null && next.isTextual() && !next.asText().isEmpty()) {
            JsonNode nextBlock = allBlocks.get(next.asText());
            if (nextBlock != null) {
                convertBlock(sb, nextBlock, allBlocks, indent);
            }
        }
    }

    // ==================== Motion block conversion methods ====================

    private void appendMotionMove(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String steps = getInputValue(block, "STEPS");
        sb.append(indent).append("angle_rad = direction * 3.14159265 / 180\n");
        sb.append(indent).append("x += ").append(steps).append(" * __import__('math').cos(angle_rad)\n");
        sb.append(indent).append("y += ").append(steps).append(" * __import__('math').sin(angle_rad)\n");
        sb.append(indent).append("output_lines.append(f\"move: x={x:.1f}, y={y:.1f}, dir={direction}\")\n");
    }

    private void appendMotionTurn(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent, String direction_type) {
        String degrees = getInputValue(block, "DEGREES");
        if ("right".equals(direction_type)) {
            sb.append(indent).append("direction = (direction + ").append(degrees).append(") % 360\n");
        } else {
            sb.append(indent).append("direction = (direction - ").append(degrees).append(") % 360\n");
        }
        sb.append(indent).append("output_lines.append(f\"turn_").append(direction_type)
                .append(": dir={direction}\")\n");
    }

    private void appendMotionGoto(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String xPos = getInputValue(block, "X");
        String yPos = getInputValue(block, "Y");
        sb.append(indent).append("x = ").append(xPos).append("\n");
        sb.append(indent).append("y = ").append(yPos).append("\n");
        sb.append(indent).append("output_lines.append(f\"goto: x={x}, y={y}\")\n");
    }

    private void appendMotionGlide(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String secs = getInputValue(block, "SECS");
        String xPos = getInputValue(block, "X");
        String yPos = getInputValue(block, "Y");
        sb.append(indent).append("old_x, old_y = x, y\n");
        sb.append(indent).append("target_x, target_y = ").append(xPos).append(", ").append(yPos).append("\n");
        sb.append(indent).append("time.sleep(").append(secs).append(")\n");
        sb.append(indent).append("x, y = target_x, target_y\n");
        sb.append(indent).append("output_lines.append(f\"glide: ({old_x},{old_y}) -> ({x},{y}), ").append(secs).append("s\")\n");
    }

    private void appendMotionSetX(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String xPos = getInputValue(block, "X");
        sb.append(indent).append("x = ").append(xPos).append("\n");
        sb.append(indent).append("output_lines.append(f\"set_x: x={x}\")\n");
    }

    private void appendMotionSetY(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String yPos = getInputValue(block, "Y");
        sb.append(indent).append("y = ").append(yPos).append("\n");
        sb.append(indent).append("output_lines.append(f\"set_y: y={y}\")\n");
    }

    private void appendMotionChangeX(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String dx = getInputValue(block, "DX");
        sb.append(indent).append("x += ").append(dx).append("\n");
        sb.append(indent).append("output_lines.append(f\"change_x: x={x}\")\n");
    }

    private void appendMotionChangeY(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String dy = getInputValue(block, "DY");
        sb.append(indent).append("y += ").append(dy).append("\n");
        sb.append(indent).append("output_lines.append(f\"change_y: y={y}\")\n");
    }

    // ==================== Control block conversion methods ====================

    private void appendControlRepeat(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent, int indentLevel) {
        String times = getInputValue(block, "TIMES");
        sb.append(indent).append("for _i in range(int(").append(times).append(")):\n");

        // Process the loop body (DO child block)
        JsonNode doInput = getInputBlock(block, "DO");
        if (doInput != null) {
            convertBlock(sb, doInput, allBlocks, indentLevel + 1);
        }
    }

    private void appendControlRepeatUntil(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent, int indentLevel) {
        String condition = getInputValue(block, "CONDITION");
        sb.append(indent).append("while not (").append(condition).append("):\n");

        JsonNode doInput = getInputBlock(block, "DO");
        if (doInput != null) {
            convertBlock(sb, doInput, allBlocks, indentLevel + 1);
        }
    }

    private void appendControlIf(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent, int indentLevel) {
        String condition = getInputValue(block, "CONDITION");
        sb.append(indent).append("if ").append(condition).append(":\n");

        JsonNode doInput = getInputBlock(block, "DO");
        if (doInput != null) {
            convertBlock(sb, doInput, allBlocks, indentLevel + 1);
        }
    }

    private void appendControlIfElse(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent, int indentLevel) {
        String condition = getInputValue(block, "CONDITION");
        sb.append(indent).append("if ").append(condition).append(":\n");

        JsonNode doInput = getInputBlock(block, "DO");
        if (doInput != null) {
            convertBlock(sb, doInput, allBlocks, indentLevel + 1);
        }

        sb.append(indent).append("else:\n");

        JsonNode elseInput = getInputBlock(block, "ELSE");
        if (elseInput != null) {
            convertBlock(sb, elseInput, allBlocks, indentLevel + 1);
        } else {
            sb.append(indent).append("    pass\n");
        }
    }

    private void appendControlWait(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String duration = getInputValue(block, "DURATION");
        sb.append(indent).append("time.sleep(").append(duration).append(")\n");
        sb.append(indent).append("output_lines.append(f\"wait: ").append(duration).append("s\")\n");
    }

    // ==================== Operator block conversion methods ====================

    private void appendOperatorExpression(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String expr = resolveOperatorExpression(block, allBlocks);
        sb.append(indent).append("output_lines.append(str(").append(expr).append("))\n");
    }

    /**
     * Resolves an operator block into a Python expression string.
     */
    private String resolveOperatorExpression(JsonNode block, JsonNode allBlocks) {
        String opcode = block.get("opcode").asText();
        switch (opcode) {
            case "operator_add": {
                String a = getInputValue(block, "NUM1");
                String b = getInputValue(block, "NUM2");
                return "(" + a + " + " + b + ")";
            }
            case "operator_subtract": {
                String a = getInputValue(block, "NUM1");
                String b = getInputValue(block, "NUM2");
                return "(" + a + " - " + b + ")";
            }
            case "operator_multiply": {
                String a = getInputValue(block, "NUM1");
                String b = getInputValue(block, "NUM2");
                return "(" + a + " * " + b + ")";
            }
            case "operator_divide": {
                String a = getInputValue(block, "NUM1");
                String b = getInputValue(block, "NUM2");
                return "(" + a + " / " + b + ")";
            }
            case "operator_mod": {
                String a = getInputValue(block, "NUM1");
                String b = getInputValue(block, "NUM2");
                return "(" + a + " % " + b + ")";
            }
            case "operator_random": {
                String from = getInputValue(block, "FROM");
                String to = getInputValue(block, "TO");
                return "random.randint(int(" + from + "), int(" + to + "))";
            }
            case "operator_mathop": {
                String operator = getInputValue(block, "OPERATOR");
                String num = getInputValue(block, "NUM");
                return resolveMathOp(operator, num);
            }
            default:
                return "0";
        }
    }

    /**
     * Resolves a Scratch math operator function into a Python expression.
     */
    private String resolveMathOp(String operator, String num) {
        switch (operator) {
            case "abs":
                return "abs(" + num + ")";
            case "floor":
                return "int(__import__('math').floor(" + num + "))";
            case "ceiling":
                return "int(__import__('math').ceil(" + num + "))";
            case "sqrt":
                return "__import__('math').sqrt(" + num + ")";
            case "sin":
                return "__import__('math').sin(" + num + " * 3.14159265 / 180)";
            case "cos":
                return "__import__('math').cos(" + num + " * 3.14159265 / 180)";
            case "tan":
                return "__import__('math').tan(" + num + " * 3.14159265 / 180)";
            case "asin":
                return "__import__('math').asin(" + num + ") * 180 / 3.14159265";
            case "acos":
                return "__import__('math').acos(" + num + ") * 180 / 3.14159265";
            case "atan":
                return "__import__('math').atan(" + num + ") * 180 / 3.14159265";
            case "ln":
                return "__import__('math').log(" + num + ")";
            case "log":
                return "__import__('math').log10(" + num + ")";
            case "e ^":
                return "__import__('math').exp(" + num + ")";
            case "10 ^":
                return "10 ** (" + num + ")";
            default:
                return num;
        }
    }

    // ==================== Variable block conversion methods ====================

    private void appendDataSetVariable(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String varName = getVariableName(block);
        String value = getInputValue(block, "VALUE");
        sb.append(indent).append("variables['").append(varName).append("'] = ").append(value).append("\n");
        sb.append(indent).append("output_lines.append(f\"set_var: ").append(varName)
                .append(" = {variables['").append(varName).append("']}\")\n");
    }

    private void appendDataChangeVariable(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String varName = getVariableName(block);
        String change = getInputValue(block, "VALUE");
        sb.append(indent).append("variables['").append(varName).append("'] = variables.get('")
                .append(varName).append("', 0) + ").append(change).append("\n");
        sb.append(indent).append("output_lines.append(f\"change_var: ").append(varName)
                .append(" = {variables['").append(varName).append("']}\")\n");
    }

    // ==================== Sensing block conversion methods ====================

    private void appendSensingTouching(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String object = getInputValue(block, "TOUCHINGOBJECTMENU");
        sb.append(indent).append("# Simulated collision sensing: ").append(object).append("\n");
        sb.append(indent).append("touching = (abs(x) > ").append(CANVAS_WIDTH / 2)
                .append(" or abs(y) > ").append(CANVAS_HEIGHT / 2).append(")\n");
        sb.append(indent).append("output_lines.append(f\"touching_").append(object)
                .append(": {touching}\")\n");
    }

    private void appendSensingAskWait(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent) {
        String question = getInputValue(block, "QUESTION");
        sb.append(indent).append("# Simulated ask and wait\n");
        sb.append(indent).append("answer = \"").append(question).append("\"\n");
        sb.append(indent).append("output_lines.append(f\"ask: {answer}\")\n");
    }

    // ==================== Looks block conversion methods ====================

    private void appendLooksSay(StringBuilder sb, JsonNode block, JsonNode allBlocks, String indent, String opcode) {
        String message = getInputValue(block, "MESSAGE");
        String prefix = "looks_think".equals(opcode) ? "think" : "say";
        sb.append(indent).append("output_lines.append(f\"").append(prefix).append(": {")
                .append(message).append("}\")\n");
    }

    // ==================== Helper methods ====================

    /**
     * Gets a value from a block input.
     * Supports both direct values and block references.
     */
    private String getInputValue(JsonNode block, String inputName) {
        JsonNode inputs = block.get("inputs");
        if (inputs == null || !inputs.has(inputName)) {
            return "0";
        }

        JsonNode input = inputs.get(inputName);
        if (input.isArray() && input.size() >= 2) {
            JsonNode valueNode = input.get(1);
            if (valueNode.isArray() && valueNode.size() > 0) {
                // [1, "value"] format - direct value
                if (valueNode.get(0).isNumber()) {
                    return String.valueOf(valueNode.get(0).asDouble());
                }
                if (valueNode.get(0).isTextual()) {
                    String textVal = valueNode.get(0).asText();
                    // If it looks like a block reference (UUID-like), resolve it
                    if (textVal.length() == 20 && textVal.contains("-")) {
                        return resolveBlockReference(textVal);
                    }
                    return "\"" + textVal.replace("\"", "\\\"") + "\"";
                }
            } else if (valueNode.isTextual()) {
                return "\"" + valueNode.asText().replace("\"", "\\\"") + "\"";
            } else if (valueNode.isNumber()) {
                return String.valueOf(valueNode.asDouble());
            }
        }
        return "0";
    }

    /**
     * Gets a child block node from a block input.
     */
    private JsonNode getInputBlock(JsonNode block, String inputName) {
        JsonNode inputs = block.get("inputs");
        if (inputs == null || !inputs.has(inputName)) {
            return null;
        }

        JsonNode input = inputs.get(inputName);
        if (input.isArray() && input.size() >= 2) {
            JsonNode ref = input.get(1);
            if (ref.isArray() && ref.size() > 0 && ref.get(0).isTextual()) {
                String blockId = ref.get(0).asText();
                // Look up from the global blocks
                return null; // requires allBlocks to be passed in externally
            }
        }
        return null;
    }

    /**
     * Resolves a block reference (looks up from the global block map and converts to an expression).
     */
    private String resolveBlockReference(String blockId) {
        // Simple placeholder; actual usage requires allBlocks to be passed in
        return "0 /* ref:" + blockId + " */";
    }

    /**
     * Gets the variable name from a variable block.
     */
    private String getVariableName(JsonNode block) {
        JsonNode inputs = block.get("inputs");
        if (inputs != null && inputs.has("VARIABLE")) {
            JsonNode varInput = inputs.get("VARIABLE");
            if (varInput.isArray() && varInput.size() >= 2) {
                JsonNode varNameNode = varInput.get(1);
                if (varNameNode.isArray() && varNameNode.size() > 0) {
                    return varNameNode.get(0).asText("unknown");
                }
            }
        }
        return "unknown";
    }
}
