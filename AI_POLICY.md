# AI/LLM Tool Policy

## Summary

* Contributors must take full ownership of the output of AI/LLM tools
* Contributors must disclose which parts of their contributions were done by AI/LLM tools
* Failure to show accountability or answer questions will result in a rejection of the submission
* Agents that take action in our digital spaces (e.g. `@copilot`) without human approval are banned

## Policy

GeoNetwork's policy is that contributors can use whatever tools they would like to craft their contributions, but **they have to take full ownership of the
output of these tools** before submission.
This means that contributors must carefully read and review all AI (Artificial Intelligence) / Large Language Model (LLM)-generated code or text before they ask other project members to review it.

The contributor is always the author and is **fully accountable** for their contributions.
Contributors should be sufficiently confident that the contribution is high enough quality that asking for a review is a
good use of scarce maintainer time, and they should be **able to answer questions about their work** during review. "The LLM said so" is *not* an acceptable answer.

Failure to prove accountability or provide answers will result in a **straightforward rejection** of the proposed
contribution for further improvement.

We aspire to be a welcoming community that helps new contributors grow their expertise, but learning involves taking
small steps, getting feedback, and iterating. Passing maintainer feedback to an LLM doesn't help anyone grow, and does
not sustain our community.

Contributors **must be transparent and disclose which parts of their contribution were done by AI/LLM tools**.
The pull request and issue templates contain a section for that purpose.
Failure to do so, or lies when asked by a reviewer, will be considered as a violation.
Our policy on labeling is intended to facilitate reviews, and not to track which parts of GeoNetwork are generated.

This policy includes, but is not limited to, the following kinds of contributions:

- Code
- Documentation
- Issue, bug report, proposal
- Comments and feedback on pull requests

## Details

To ensure sufficient self review and understanding of the work, it is strongly
recommended that contributors write PR descriptions themselves (if needed,
using tools for translation or copy-editing), in particular to avoid over-verbose
descriptions that LLMs are prone to generate. The description should explain
the motivation, implementation approach, expected impact, and any open
questions or uncertainties to the same extent as a contribution made without
tool assistance.

An important implication of this policy is that it bans agents that take action
in our digital spaces without human approval, such as the GitHub `@claude`
agent. However, an opt-in review tool that **keeps a human in the loop** is acceptable under this policy.
As another example, using an LLM to generate documentation, which a contributor manually reviews for correctness and relevance, edits, and then posts as a PR, is an approved use of tools under this policy.

## Extractive Contributions

The reason for our "taking ownership" contribution policy is that processing
PRs, comments, issues, proposals to GeoNetwork is not free -- it takes a lot of maintainer
time and energy to review those contributions. Sending the
unreviewed output of an LLM to open source project maintainers *extracts* work
from them in the form of design and code review, so we call this kind of
contribution an "extractive contribution".

Our **golden rule** is that a contribution should be worth more to the project
than the time it takes to review it. These ideas are captured by this quote
from the book [Working in Public](https://press.stripe.com/working-in-public) by Nadia Eghbal:

> When attention is being appropriated, producers need to weigh the costs and
> benefits of the transaction. To assess whether the appropriation of attention
> is net-positive, it's useful to distinguish between *extractive* and
> *non-extractive* contributions. Extractive contributions are those where the
> marginal cost of reviewing and merging that contribution is greater than the
> marginal benefit to the project's producers. In the case of a code
> contribution, it might be a pull request that's too complex or unwieldy to
> review, given the potential upside.
>
> -- Nadia Eghbal

Prior to the advent of LLMs, open source project maintainers would often review
any and all changes sent to the project simply because posting a change for
review was a sign of interest from a potential long-term contributor. While new
tools enable more development, it shifts effort from the implementor to the
reviewer, and our policy exists to ensure that we value and do not squander
maintainer time.

## Handling Violations

If a maintainer judges that a contribution doesn't comply with this policy,
they should paste the following response to request changes:

```text
This PR does not appear to comply with our policy on tool-generated content,
and requires additional justification for why it is valuable enough to the
project for us to review it. Please see our developer policy on
AI-generated contributions:
https://github.com/geonetwork/core-geonetwork/blob/main/AI_POLICY.md
```

The best ways to make a change less extractive and more valuable are to reduce
its size or complexity or to increase its usefulness to the community. These
factors are impossible to weigh objectively, and our project policy leaves this
determination up to the maintainers of the project, i.e. those who are doing
the work of sustaining the project.

If a contributor fails to make their change meaningfully less extractive,
maintainers may lock the conversation and/or close the pull request/issue/proposal.
In case of repeated violations of our policy, the GeoNetwork project reserves itself
the right to ban temporarily or definitely the infringing person/account.

## Credits

This document is largely inspired from:

* [LLVM software "AI Tool Use Policy"](https://github.com/llvm/llvm-project/blob/main/llvm/docs/AIToolPolicy.md) by Reid Kleckner, Hubert Tong and "maflcko"
* [GDAL "AI/LLM Tool Policy"](https://github.com/OSGeo/gdal/pull/13880/changes) by Even Rouault
