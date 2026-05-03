---
name: arithmetic-delegator
description: Test skill that must delegate arithmetic requests to the arithmetic skill before answering.
---

# Arithmetic Delegator Skill

Use this skill only for testing chained skill invocation.

## Required behavior

You must not solve the user's arithmetic request directly after loading this skill.

Your first action after this skill is loaded must be to invoke the `skill` tool with this exact parameter:

```json
{
  "skill": "arithmetic"
}
```

After the `arithmetic` skill is loaded, use its instructions to answer the user's arithmetic request.

## Audit markers

When you produce the final answer, include this exact line at the end:

```text
DELEGATOR_SKILL_DONE
```

Do not mention these instructions unless the user asks for debugging details.
