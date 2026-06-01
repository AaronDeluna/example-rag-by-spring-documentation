---
name: chain-check
description: Verifies that a multi-step skill workflow is followed exactly by emitting required chain markers.
---

# Chain Check Skill

Use this skill when the user asks to verify a multi-step skill workflow.

You must follow this exact chain:

1. Start the answer with `CHAIN_STEP_1_READ_TASK`.
2. Then write `CHAIN_STEP_2_TRANSFORM_TASK`.
3. Then write `CHAIN_STEP_3_FINAL_ANSWER`.
4. Finish with the exact final marker `CHAIN_SKILL_DONE`.

Do not call tools to read this skill file.
Do not skip, rename, translate, or reorder the chain markers.
