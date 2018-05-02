using System;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Host;

namespace MinicraftLog
{
    public static class LogPlayers
    {
        [FunctionName("LogPlayers")]
        public static void Run([TimerTrigger("*/15 * * * * *", RunOnStartup = true)]TimerInfo myTimer, TraceWriter log)
        {

            var ipstring = Environment.GetEnvironmentVariable("ServerIPs", EnvironmentVariableTarget.Process);

            var ipsWithPort = ipstring.Split(';');
            
            foreach(var serverIpAndPort in ipsWithPort)
            {
                RunPerServer(serverIpAndPort, log);
            }
        }

        private static void RunPerServer(string serverIpAndPort, TraceWriter log)
        {
            var split = serverIpAndPort.Split(':');
            var ip = split[0];
            var port = split[1];

            var minestat = new MineStat(ip, ushort.Parse(port));

            try
            {
                LogStuff.Run(new InputObject
                {
                    Capacity = minestat.MaximumPlayers,
                    CurrentPlayers = minestat.CurrentPlayers,
                    ServerIsUp = minestat.ServerUp.ToString(),
                    ServerIp = ip
                }, log);
            }
            catch
            {
                log.Error("Failed to upload to log analytics");
            }

            log.Info($"Server {ip} is up: {minestat.ServerUp}");
            log.Info($"Players: {minestat.CurrentPlayers}");

        }
    }
}
