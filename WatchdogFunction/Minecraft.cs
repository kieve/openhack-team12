using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace MinicraftLog
{
    public class MineStat

    {

        const ushort dataSize = 512; // this will hopefully suffice since the MotD should be <=59 characters

        const ushort numFields = 6;  // number of values expected from server



        public string Address { get; set; }

        public ushort Port { get; set; }

        public string Motd { get; set; }

        public string Version { get; set; }

        public int CurrentPlayers { get; set; }

        public int MaximumPlayers { get; set; }

        public bool ServerUp { get; set; }

        public long Delay { get; set; }



        public MineStat(string address, ushort port)

        {

            var rawServerData = new byte[dataSize];



            Address = address;

            Port = port;



            try

            {

                // ToDo: Add timeout


                var tcpclient = new TcpClient();

                tcpclient.Connect(address, port);

                var stream = tcpclient.GetStream();

                var payload = new byte[] { 0xFE, 0x01 };

                stream.Write(payload, 0, payload.Length);

                stream.Read(rawServerData, 0, dataSize);

                tcpclient.Close();

            }

            catch (Exception)

            {

                ServerUp = false;

                return;

            }



            if (rawServerData == null || rawServerData.Length == 0)

            {

                ServerUp = false;

            }

            else

            {

                var serverData = Encoding.Unicode.GetString(rawServerData).Split("\u0000\u0000\u0000".ToCharArray());

                if (serverData != null && serverData.Length >= numFields)

                {

                    ServerUp = true;

                    Version = serverData[2];

                    Motd = serverData[3];

                    int curPlayers = 0;
                    bool success = Int32.TryParse(serverData[4], out curPlayers);
                    if (success) {
                        CurrentPlayers = curPlayers;
                    } else {
                        CurrentPlayers = 0;
                    }

                    int maxPlayers = 0;
                    success = Int32.TryParse(serverData[5], out maxPlayers);
                    if (success) {
                        MaximumPlayers = maxPlayers;
                    } else {
                        MaximumPlayers = 0;
                    }

                }

                else

                {

                    ServerUp = false;

                }

            }

        }


    }

}
