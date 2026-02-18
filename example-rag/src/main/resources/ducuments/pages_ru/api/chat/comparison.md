# Сравнение моделей чата

// :YES: ![width=16](yes.svg)
// :NO: ![width=12](no.svg)

В этой таблице сравниваются различные модели чата, поддерживаемые Spring AI, с описанием их возможностей:

- xref:api/multimodality.adoc[Мультимодальность]: Типы входных данных, которые модель может обрабатывать (например, текст, изображение, аудио, видео).
- xref:api/tools.adoc[Инструменты/Вызов функций]: Поддерживает ли модель вызов функций или использование инструментов.
- Потоковая передача: Предлагает ли модель потоковые ответы.
- Повторная попытка: Поддержка механизмов повторной попытки.
- xref:observability/index.adoc[Наблюдаемость]: Функции для мониторинга и отладки.
- xref:api/structured-output-converter.adoc#_built_in_json_mode[Встроенный JSON]: Нативная поддержка JSON-выхода.
- Локальное развертывание: Можно ли запустить модель локально.
- Совместимость с API OpenAI: Совместима ли модель с API OpenAI.

[cols="10,5,1,1,1,1,1,1,1", stripes=even]
| Провайдер | Мультимодальность ^| Инструменты/Функции ^| Потоковая передача ^| Повторная попытка ^| Наблюдаемость ^| Встроенный JSON ^| Локально ^| Совместимость с API OpenAI

| xref::api/chat/anthropic-chat.adoc[Anthropic Claude]  | текст, pdf, изображение ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg)
| xref::api/chat/azure-openai-chat.adoc[Azure OpenAI]  | текст, изображение ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/deepseek-chat.adoc[DeepSeek (OpenAI-proxy)]  | текст ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/google-genai-chat.adoc[Google GenAI]  | текст, pdf, изображение, аудио, видео ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg)
| xref::api/chat/vertexai-gemini-chat.adoc[Google VertexAI Gemini]  | текст, pdf, изображение, аудио, видео ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/groq-chat.adoc[Groq (OpenAI-proxy)]  | текст, изображение ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/huggingface.adoc[HuggingFace]  | текст ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg)
| xref::api/chat/mistralai-chat.adoc[Mistral AI]  | текст, изображение, аудио ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/minimax-chat.adoc[MiniMax]  | текст ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/moonshot-chat.adoc[Moonshot AI]  | текст ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg)
| xref::api/chat/nvidia-chat.adoc[NVIDIA (OpenAI-proxy)]  | текст, изображение ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/oci-genai/cohere-chat.adoc[OCI GenAI/Cohere] | текст ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg)
| xref::api/chat/ollama-chat.adoc[Ollama]  | текст, изображение ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/openai-sdk-chat.adoc[OpenAI SDK (Официальный)] a| Вход: текст, изображение, аудио; Выход: текст, аудио ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/openai-chat.adoc[OpenAI] a| Вход: текст, изображение, аудио; Выход: текст, аудио ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/perplexity-chat.adoc[Perplexity (OpenAI-proxy)]  | текст ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg)
| xref::api/chat/qianfan-chat.adoc[QianFan]  | текст ^a| ![width=12](no.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg)
| xref::api/chat/zhipuai-chat.adoc[ZhiPu AI]  | текст, изображение, документы ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg)
| xref::api/chat/bedrock-converse.adoc[Amazon Bedrock Converse] | текст, изображение, видео, документы (pdf, html, md, docx ...) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=16](yes.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg) ^a| ![width=12](no.svg)
