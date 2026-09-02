package com.example.knowledgesphere.chat.service;

import com.example.knowledgesphere.ai.prompt.PromptService;
import com.example.knowledgesphere.chat.dto.ChatResponse;
import com.example.knowledgesphere.conversation.service.ConversationService;
import com.example.knowledgesphere.document.service.DocumentRetrievalService;
import com.example.knowledgesphere.entity.Conversation;
import com.example.knowledgesphere.entity.MessageRole;
import com.example.knowledgesphere.entity.RetrievedDocument;
import com.example.knowledgesphere.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final ConversationService conversationService;
    private final PromptService promptService;
    private final DocumentRetrievalService retrievalService;


    @Transactional
    public ChatResponse ask(
            String question,
            Long conversationId,
            User user
    ) {

        log.info("==========================================");
        log.info("PROCESSING CHAT REQUEST");
        log.info("User      : {}", user.getEmail());
        log.info("Question  : {}", question);
        log.info("Conversation ID : {}", conversationId);
        log.info("==========================================");


        /*
         * ---------------------------------------------------------
         * 1. GET OR CREATE CONVERSATION
         * ---------------------------------------------------------
         */

        Conversation conversation;

        if (conversationId != null) {

            conversation =
                    conversationService.getConversationForUser(
                            conversationId,
                            user
                    );

        } else {

            String title =
                    question.length() > 30
                            ? question.substring(0, 30) + "..."
                            : question;

            conversation =
                    conversationService.createConversation(
                            user,
                            title
                    );
        }


        /*
         * ---------------------------------------------------------
         * 2. SAVE USER MESSAGE
         * ---------------------------------------------------------
         */

        conversationService.saveMessage(
                conversation,
                MessageRole.USER,
                question
        );


        try {

            /*
             * -----------------------------------------------------
             * 3. RETRIEVE DOCUMENT CHUNKS
             * -----------------------------------------------------
             */

            List<RetrievedDocument> retrievedDocuments =
                    retrievalService.retrieve(question);


            log.info("------------------------------------------");
            log.info("RETRIEVED CHUNKS : {}", retrievedDocuments.size());
            log.info("------------------------------------------");


            if (retrievedDocuments.isEmpty()) {

                String answer =
                        "I couldn't find this information in the uploaded documents.";

                conversationService.saveMessage(
                        conversation,
                        MessageRole.ASSISTANT,
                        answer
                );

                return ChatResponse.builder()
                        .conversationId(conversation.getId())
                        .response(answer)
                        .sources(List.of())
                        .build();
            }


            /*
             * -----------------------------------------------------
             * 4. LOG ALL RETRIEVED CHUNKS
             * -----------------------------------------------------
             */

            for (int i = 0; i < retrievedDocuments.size(); i++) {

                RetrievedDocument doc =
                        retrievedDocuments.get(i);

                log.info("========== CHUNK {} ==========", i + 1);

                log.info("File       : {}", doc.getFileName());
                log.info("Page       : {}", doc.getPage());
                log.info("Paragraph  : {}", doc.getParagraph());
                log.info("Heading    : {}", doc.getHeading());

                log.info("Content    : {}", doc.getContent());

                log.info("================================");
            }


            /*
             * -----------------------------------------------------
             * 5. BUILD STRUCTURED CONTEXT
             * -----------------------------------------------------
             */

            StringBuilder context =
                    new StringBuilder();


            context.append("""
                    
                    ================= RETRIEVED DOCUMENTS =================
                    
                    The following information was retrieved from the
                    uploaded documents.
                    
                    IMPORTANT:
                    - Every chunk may contain information relevant to the question.
                    - You MUST examine ALL chunks before answering.
                    - Do NOT stop after finding the first matching record.
                    - For table questions, examine ALL rows in the retrieved tables.
                    
                    """);


            for (int i = 0;
                 i < retrievedDocuments.size();
                 i++) {

                RetrievedDocument doc =
                        retrievedDocuments.get(i);


                context.append(
                        """

                        ----------------------------------------------------
                        DOCUMENT CHUNK %d
                        ----------------------------------------------------

                        File Name:
                        %s

                        Page:
                        %s

                        Paragraph:
                        %s

                        Heading:
                        %s

                        Content:
                        %s

                        ----------------------------------------------------

                        """.formatted(

                                i + 1,

                                safe(doc.getFileName()),

                                safe(doc.getPage()),

                                safe(doc.getParagraph()),

                                safe(doc.getHeading()),

                                safe(doc.getContent())
                        )
                );
            }


            /*
             * -----------------------------------------------------
             * 6. VERY STRICT RAG PROMPT
             * -----------------------------------------------------
             */

            String finalPrompt = """

                    You are a document question-answering assistant.

                    You must answer the user's question ONLY from the
                    RETRIEVED DOCUMENT CONTEXT provided below.

                    ====================================================
                    IMPORTANT RULES
                    ====================================================

                    RULE 1:
                    Read and analyze EVERY retrieved document chunk before
                    generating the answer.

                    RULE 2:
                    NEVER stop at the first matching record.

                    RULE 3:
                    If multiple records satisfy the user's question,
                    include ALL of them.

                    RULE 4:
                    For tables, inspect ALL retrieved rows and columns.

                    RULE 5:
                    Preserve the exact relationship between table columns.

                    For example:

                    Name | From | To

                    means:

                    Name = employee name
                    From = previous location
                    To = new location

                    Do NOT mix values from different rows.
                    
                    TABLE RULE:
                    
                    When answering from a table:
                    
                    1. Identify every row matching the conditions in the question.
                    2. Return all matching rows.
                    3. Do not stop after finding the first match.
                    4. Keep the value associated with its correct scheme/category.
                    5. Never combine values from different rows or columns.
                    
                    RULE 6:
                    Do not combine information from unrelated documents
                    unless the question requires information from multiple
                    documents.

                    RULE 7:
                    Do not invent names, locations, dates, numbers,
                    departments, or other information.

                    RULE 8:
                    Do not use your general knowledge.

                    RULE 9:
                    If the retrieved documents do not contain the answer,
                    respond exactly:

                    I couldn't find this information in the uploaded documents.

                    RULE 10:
                    If the question asks "who", "which employees",
                    "what records", "which people", etc., return ALL
                    matching records found in the retrieved context.

                    RULE 11:
                    If the question asks for a comparison or multiple
                    pieces of information, provide all relevant values.

                    RULE 12:
                    After answering, provide the source information.

                    ====================================================
                    ANSWER FORMAT
                    ====================================================

                    MULTIPLE MATCH RULE:

                        The retrieved context may contain multiple chunks, sections, tables,
                        records, lists, or paragraphs.
                        
                        Before answering:
                        
                        1. Inspect all retrieved context.
                        2. Identify every piece of context that is relevant to the user's question.
                        3. If multiple distinct records satisfy the user's question, include all of them.
                        4. Keep information belonging to the same record, row, section, or entity together.
                        5. Never merge a value from one record with a value from another record.
                        6. Do not stop after finding the first matching answer.
                        7. Return each distinct matching result separately, using labels or context
                           naturally present in the retrieved data.
                        8. If the same answer is repeated in multiple chunks, do not repeat it unnecessarily.
                        9. Only return information supported by the retrieved context.
                        10. If an important condition is missing from the user's question and multiple
                            valid answers exist because of that missing condition, return the distinct
                            answers and clearly indicate the condition that differentiates them.
                            
                            
                            
                        You are answering from retrieved context.
                    
                        Before generating the answer:
                    
                        1. Inspect ALL retrieved chunks, not only the highest-ranked chunk.
                        2. Identify every distinct record, fact, row, section, or entry that satisfies the user's question.
                        3. If multiple distinct matches exist, return ALL of them.
                        4. Preserve the context that distinguishes each result, using information present in the retrieved content.
                        5. Keep values belonging to the same record together.
                        6. Never merge information from different records.
                        7. Do not stop after finding the first valid answer.
                        8. Remove only exact duplicate results.
                        9. If only one matching result exists, return one answer.
                        10. If multiple matching results exist, clearly present each answer separately.
                        11. Do not invent missing values. Only use values explicitly present in the retrieved context.    
                    ====================================================
                    RETRIEVED DOCUMENT CONTEXT
                    ====================================================

                    %s

                    ====================================================
                    USER QUESTION
                    ====================================================

                    %s

                    ====================================================
                    FINAL CHECK BEFORE ANSWERING
                    ====================================================

                    Before generating the final answer, verify:

                    1. Did I inspect every retrieved chunk?
                    2. Did I check every relevant table row?
                    3. Did I include every record that matches the question?
                    4. Did I keep values from the same row together?
                    5. Did I avoid using information outside the context?
                    6. Did I avoid inventing anything?

                    Now answer the user's question.

                    """.formatted(
                    context,
                    question
            );


            /*
             * -----------------------------------------------------
             * 7. LOG FINAL PROMPT
             * -----------------------------------------------------
             */

            log.info("========== FINAL RAG PROMPT ==========");
            log.info(finalPrompt);
            log.info("======================================");


            /*
             * -----------------------------------------------------
             * 8. CALL LLM
             * -----------------------------------------------------
             */

            String answer =
                    chatClient
                            .prompt()
                            .system(promptService.systemPrompt())
                            .user(finalPrompt)
                            .call()
                            .content();


            /*
             * -----------------------------------------------------
             * 9. CHECK RESPONSE
             * -----------------------------------------------------
             */

            if (answer == null ||
                    answer.trim().isEmpty()) {

                log.error("LLM returned EMPTY response");

                answer =
                        "I couldn't generate an answer from the retrieved documents.";
            }


            log.info("========== LLM RESPONSE ==========");
            log.info(answer);
            log.info("===================================");


            /*
             * -----------------------------------------------------
             * 10. SAVE ASSISTANT MESSAGE
             * -----------------------------------------------------
             */

            conversationService.saveMessage(
                    conversation,
                    MessageRole.ASSISTANT,
                    answer
            );


            /*
             * -----------------------------------------------------
             * 11. RETURN RESPONSE
             * -----------------------------------------------------
             */

            List<RetrievedDocument> sources =
                    retrievedDocuments
                            .stream()
                            .limit(5)
                            .toList();


            return ChatResponse.builder()

                    .conversationId(
                            conversation.getId()
                    )

                    .response(
                            answer
                    )

                    .sources(
                            sources
                    )

                    .build();


        } catch (Exception e) {

            log.error(
                    "=========================================="
            );

            log.error(
                    "CHAT / LLM ERROR"
            );

            log.error(
                    "Question : {}",
                    question
            );

            log.error(
                    "Conversation : {}",
                    conversation.getId()
            );

            log.error(
                    "Error : {}",
                    e.getMessage(),
                    e
            );

            log.error(
                    "=========================================="
            );


            /*
             * Do NOT save the error as an assistant message.
             *
             * Otherwise your chat history will contain:
             *
             * USER -> question
             * ASSISTANT -> Sorry, error
             *
             * which makes debugging/history confusing.
             */

            return ChatResponse.builder()

                    .conversationId(
                            conversation.getId()
                    )

                    .response(
                            "Sorry, I encountered an error while generating the answer."
                    )

                    .sources(
                            List.of()
                    )

                    .build();
        }
    }


    /*
     * ---------------------------------------------------------
     * SAFE STRING HELPER
     * ---------------------------------------------------------
     */

    private String safe(Object value) {

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }
}