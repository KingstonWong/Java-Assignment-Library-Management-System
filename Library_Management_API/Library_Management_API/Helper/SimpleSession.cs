using System.Collections.Concurrent;

namespace Library_Management_API.Helper
{
    public class SimpleSession
    {
        // Token => employee_id
        private static ConcurrentDictionary<string, int> sessions = new();

        // Add a new session
        public static string AddSession(int memberID)
        {
            string token = Guid.NewGuid().ToString();
            sessions[token] = memberID;
            return token;
        }

        // Get employee_id by token
        public static int? GetEmployeeId(string token)
        {
            if (sessions.TryGetValue(token, out int memberID))
                return memberID;
            return null;
        }

        // Remove a session
        public static void RemoveSession(string token)
        {
            sessions.TryRemove(token, out _);
        }
    }
}
