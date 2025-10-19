package apm;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import de.kherud.llama.InferenceParameters;
import de.kherud.llama.LlamaModel;
import de.kherud.llama.LlamaOutput;
import de.kherud.llama.ModelParameters;
import de.kherud.llama.args.MiroStat;

/**
 * This class represents the interface to the LLM.
 *
 * @author Dirk Pawlaszczyk
 */
public class LLMMgr {

    private static LLMMgr instance;
    private static List<String> documents = new ArrayList<>();
    private static ModelParameters modelParams;
    private static LlamaModel model;
    private static String prompt;

    private static String model_path;
    private static int gpu_layers;
    private static float temperature;
    private static float top_p;
    private static int top_k;
    //private static int max_tokens;
    private static float frequency_penalty;
    private static float presence_penalty;
    private static String system_prompt;

    /*
     *  We have one static LLM for the application.
     *  There is only one instance of this class (Singleton pattern).
     */
    static{
        instance = new LLMMgr();
        instance.prepareLLM();
    }

    /**
     * Use this method to get the instance object of the manager.
     * @return Manager instance
     */
    public static LLMMgr getInstance(){
        return instance;
    }

    private void loadConfig() {
        if (!Files.exists(Global.configPath)) {

            gpu_layers = 17;

            return;
        }

        Properties props = new Properties();
        try (InputStream input = new FileInputStream(Global.configPath.toFile())) {
            props.load(input);
            model_path = props.getProperty("model_path", "");
            gpu_layers = Integer.parseInt(props.getProperty("gpu_layers", "17"));
            temperature = Float.parseFloat(props.getProperty("temperature", "0.7"));
            top_p = Float.parseFloat(props.getProperty("top_p", "0.9"));
            top_k = Integer.parseInt(props.getProperty("top_k", "40"));
            //max_tokens = Integer.parseInt(props.getProperty("max_tokens", "4096"));
            frequency_penalty = Float.parseFloat(props.getProperty("frequency_penalty", "0.0"));
            presence_penalty = Float.parseFloat(props.getProperty("presence_penalty", "0.0"));
            system_prompt = props.getProperty("system_prompt", "Put your request here...");

            System.out.println("Configuration loaded from: " + Global.configPath);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }


    /**
     * This method is used to prepare the LLM model.
     */
    private void prepareLLM() {

        loadConfig();

        // Begin by setting gpuLayers to the total number of layers in your model.
        // For example, a 7B model often has 25-35 layers.
        // If your Mac has sufficient unified memory, this setting should provide the best performance.
        // "/Users/pawel/Library/Application Support/nomic.ai/GPT4All/qwen2.5-coder-7b-instruct-q4_0.gguf"

         modelParams = new ModelParameters()
                .setModel(model_path)
                .enableEmbedding()
                .setGpuLayers(gpu_layers)
                 .setTopP(top_p)
                 .setTopK(top_k)
                 .setFrequencyPenalty(frequency_penalty)
                 .setPresencePenalty(presence_penalty);


        //We're going to embed three storyboard examples
        try (LlamaModel model = new LlamaModel(modelParams)) {

            LLMMgr.model = model;

        }

        // Now, everything should be ready ;-): we have the embeddings loaded, the model parameters are set and the
        // model itself is prepared to go
    }

    private String example(){

       return " Here is an example for the syntax:"+
               "sequenceDiagram \n" +
                "title My Conversation\n" +
                "%% Example for a APM storyboard.\n" +
                "%% will take place on mobile phones. \n" +
                "%% This example defines the activities of two phone users (Alice and Bob). \n" +
                "%% Both send messages to each other. \n" +
                "actor A as Alice\n" +
                "actor B as Bob\n" +
                "%% Send a message to B over WhatsApp.\n" +
                "B->>A: send[WhatsApp]\"How are you?\"\n" +
                "A->>A: read messages[WhatsApp]\n" +
                "%% respond to the message from A.\n" +
                "A->>B: send[WhatsApp]\"I'm fine.\"\n" +
                "A->>A: idle(1)\n" +
                "%% change your current location to the new location 'HOME'\n" +
                "A->>A: mock location[WORK].\n" +
                "%% checkout for new messages in WhatsApp\n" +
                "A->>A: read messages[WhatsApp]\n" +
                "%% Do some random (white noise) activities.\n" +
                "B->>B: random(10)\n" +
                "%% browse a website. \n" +
                "A->>A: browse \"https://hs-mittweida.de\"\n" +
                "%% send a mail to B\n" +
                "A->>B: send mail\n" +
                "%% $receiver:\"mrx@example.com\"\n" +
                "%% $subject:\"urgent message\"\n" +
                "%% $msg:\"hi, this mail is for you. Kind Regards \"\n" +
                "A->>A: read mails\n" +
                "%% Send a message to B over WhatsApp.\n" +
                "B->>A: send[WhatsApp]\"I could definitely see that.\"\n" +
                "A->>A: read messages[WhatsApp]\n" +
                "%% respond to the message from A.\n" +
                "A->>A: take photo\n" +
                "%% Do a phone call. \n" +
                "A->>B:call 45s ";

    }


    /**
     * This method starts the inference process.
     *
     * @param newprompt the prompt
     */
    public String run(String newprompt){


               prompt = "\nUser: ";
               prompt += newprompt;
               prompt += example();
               prompt += "\nLLM: ";

                InferenceParameters inferParams = new InferenceParameters(prompt)
                        .setTemperature(temperature)
                        .setPenalizeNl(true)
                        .setMiroStat(MiroStat.V2)
                        .setStopStrings("User:");

                String answer = "";
                for (LlamaOutput output: model.generate(inferParams)) {
                    prompt += output;
                    answer += output;
                }

                return answer;

    }


}