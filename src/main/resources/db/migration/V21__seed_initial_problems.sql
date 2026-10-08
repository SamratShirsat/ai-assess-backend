INSERT INTO problems (
    title,
    description,
    difficulty,
    topic,
    constraints,
    input_format,
    output_format,
    starter_code,
    expected_time_complexity,
    expected_space_complexity,
    created_at,
    updated_at
) VALUES
(
    'Two Sum',
    'Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. You may assume that each input would have exactly one solution, and you may not use the same element twice.',
    'EASY',
    'ARRAY',
    '2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9\nOnly one valid answer exists.',
    'Line 1: Space-separated integers representing nums\nLine 2: Single integer target',
    'Two space-separated indices representing the answer in ascending order',
    'class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Write your solution here\n        return new int[]{};\n    }\n}',
    'O(n)',
    'O(n)',
    NOW(),
    NOW()
),
(
    'Valid Parentheses',
    'Given a string s containing just the characters ''('', '')'', ''{'', ''}'', ''['' and '']'', determine if the input string is valid.\n\nAn input string is valid if open brackets are closed by the same type of brackets and in the correct order.',
    'EASY',
    'STACK',
    '1 <= s.length <= 10^4\ns consists of parentheses only ''()[]{}''.',
    'A single string s containing brackets.',
    'true if valid, false otherwise.',
    'class Solution {\n    public boolean isValid(String s) {\n        // Write your solution here\n        return false;\n    }\n}',
    'O(n)',
    'O(n)',
    NOW(),
    NOW()
);