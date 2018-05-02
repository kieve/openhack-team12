using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Azure.WebJobs.Host;

namespace MinicraftLog
{
    // The input json
    public class InputObject
    {
        public String ServerIsUp { get; set; }
        //public String MaxNumberOfPlayers { get; set; }
        public int Capacity { get; set; }
        public int CurrentPlayers { get; set; }
        public string ServerIp { get; set; }
    }

    // Class for what should be sent to Log Analytics
    public class InputJson
    {
        public string ServerIsUp { get; set; }
        public int Capacity { get; set; }
        public int CurrentPlayers { get; set; }
        public string ServerIp { get; set; }
    }


    public class LogStuff
    {

        public static void Run(InputObject req, TraceWriter log)
        {

            // Update customerId to your Operations Management Suite workspace ID
            string customerId = "af9c9713-6132-4dcf-b835-5444ced60c9b";
            // For sharedKey, use either the primary or the secondary Connected Sources client authentication key   
            string sharedKey = "tAXn9XG530PyZswhK3E0okm88/NPgnWvBE6/E8RViSZy+sk11l9wA9X8BLzB9LqBBuq0ymDl4vKXuuN8vqCaxQ==";
            // LogName is name of the event type that is being submitted to Log Analytics
            string LogName = "MinecraftLog";

            // Creates the JSON object, with key/value pairs
            InputJson jsonObj = new InputJson();
            jsonObj.ServerIsUp = req?.ServerIsUp;
            jsonObj.Capacity = req.Capacity;
            jsonObj.CurrentPlayers = req.CurrentPlayers;
            jsonObj.ServerIp = req?.ServerIp;
            // Convert object to json
            var json = JsonConvert.SerializeObject(jsonObj);

            // Create a hash for the API signature
            var datestring = DateTime.UtcNow.ToString("r");
            string stringToHash = "POST\n" + json.Length + "\napplication/json\n" + "x-ms-date:" + datestring + "\n/api/logs";
            string hashedString = BuildSignature(stringToHash, sharedKey);
            string signature = "SharedKey " + customerId + ":" + hashedString;

            PostData(signature, datestring, json, customerId, LogName, log);
        }

        // Build the API signature
        public static string BuildSignature(string message, string secret)
        {
            var encoding = new System.Text.ASCIIEncoding();
            byte[] keyByte = Convert.FromBase64String(secret);
            byte[] messageBytes = encoding.GetBytes(message);
            using (var hmacsha256 = new HMACSHA256(keyByte))
            {
                byte[] hash = hmacsha256.ComputeHash(messageBytes);
                return Convert.ToBase64String(hash);
            }
        }

        // Send a request to the POST API endpoint
        public static void PostData(string signature, string date, string json, string customerId, string LogName, TraceWriter log)
        {
            // You can use an optional field to specify the timestamp from the data. If the time field is not specified, Log Analytics assumes the time is the message ingestion time
            string TimeStampField = "";
            try
            {
                string url = "https://" + customerId + ".ods.opinsights.azure.com/api/logs?api-version=2016-04-01";

                System.Net.Http.HttpClient client = new System.Net.Http.HttpClient();
                client.DefaultRequestHeaders.Add("Accept", "application/json");
                client.DefaultRequestHeaders.Add("Log-Type", LogName);
                client.DefaultRequestHeaders.Add("Authorization", signature);
                client.DefaultRequestHeaders.Add("x-ms-date", date);
                client.DefaultRequestHeaders.Add("time-generated-field", TimeStampField);

                System.Net.Http.HttpContent httpContent = new StringContent(json, Encoding.UTF8);
                httpContent.Headers.ContentType = new MediaTypeHeaderValue("application/json");
                Task<System.Net.Http.HttpResponseMessage> response = client.PostAsync(new Uri(url), httpContent);

                System.Net.Http.HttpContent responseContent = response.Result.Content;
                string result = responseContent.ReadAsStringAsync().Result;
                log.Error("Return Result: " + result);
            }
            catch (Exception excep)
            {
                log.Error("API Post Exception: " + excep.Message);
            }
        }

    }


}
